/**
 * wasm implementation of the view model instance API over @rive-app/canvas's ViewModelInstance.
 * The runtime reports changes through callbacks, which feed the flows here. Each property is wrapped
 * once per path, so looking it up again registers no further callback. Once released, lookups find
 * nothing, reads return the last value seen and writes do nothing.
 */
@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalRiveCmpApi::class)
internal class WasmRiveViewModelInstance(private val instance: RiveViewModelInstanceJs) : RiveViewModelInstance {

    private val releases = mutableListOf<() -> Unit>()
    private val adapters = mutableMapOf<String, Any>()
    private var released = false

    override fun number(path: String): RiveProperty<Float>? = cached("number", path) {
        property(
            instance.number(path),
            read = { (it as JsNumber).toDouble().toFloat() },
            write = { it.toDouble().toJsNumber() },
        )
    }

    override fun string(path: String): RiveProperty<String>? = cached("string", path) {
        property(
            instance.string(path),
            read = { (it as JsString).toString() },
            write = { it.toJsString() },
        )
    }

    override fun boolean(path: String): RiveProperty<Boolean>? = cached("boolean", path) {
        property(
            instance.boolean(path),
            read = { (it as JsBoolean).toBoolean() },
            write = { it.toJsBoolean() },
        )
    }

    override fun color(path: String): RiveProperty<Int>? = cached("color", path) {
        property(
            instance.color(path),
            // Through Long, so an ARGB value reported as unsigned wraps into an Int instead of clamping.
            read = { (it as JsNumber).toDouble().toLong().toInt() },
            write = { it.toJsNumber() },
        )
    }

    override fun enum(path: String): RiveProperty<String>? = cached("enum", path) {
        property(
            instance.enum(path),
            read = { (it as JsString).toString() },
            write = { it.toJsString() },
        )
    }

    override fun trigger(path: String): RiveTrigger? = cached("trigger", path) {
        instance.trigger(path)?.let { property ->
            releases += { property.off() }
            WasmRiveTrigger(property, isReleased = { released })
        }
    }

    /** The adapter already made for this kind of property at [path], or a new one from [create]. */
    @Suppress("UNCHECKED_CAST")
    private fun <A : Any> cached(kind: String, path: String, create: () -> A?): A? {
        if (released) return null
        val key = "$kind:$path"
        return (adapters[key] as A?) ?: create()?.also { adapters[key] = it }
    }

    private fun <T> property(property: RiveValueJs?, read: (JsAny?) -> T, write: (T) -> JsAny?): RiveProperty<T>? {
        if (property == null) return null
        releases += { property.off() }
        return WasmRiveProperty(property, read, write, isReleased = { released })
    }

    /** Removes the callbacks registered on the runtime's properties and stops touching them. */
    fun release() {
        releases.forEach { it() }
        releases.clear()
        adapters.clear()
        released = true
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
private class WasmRiveProperty<T>(
    private val property: RiveValueJs,
    private val read: (JsAny?) -> T,
    private val write: (T) -> JsAny?,
    private val isReleased: () -> Boolean,
) : RiveProperty<T> {
    private val flow = MutableStateFlow(read(property.value))

    init {
        property.on { newValue -> flow.value = read(newValue) }
    }

    override var value: T
        get() = if (isReleased()) flow.value else read(property.value)
        set(value) {
            if (isReleased()) return
            property.value = write(value)
            flow.value = value
        }

    override val valueFlow: StateFlow<T> get() = flow
}

@OptIn(ExperimentalRiveCmpApi::class)
private class WasmRiveTrigger(
    private val property: RiveTriggerJs,
    private val isReleased: () -> Boolean,
) : RiveTrigger {
    private val flow = MutableSharedFlow<Unit>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        property.on { flow.tryEmit(Unit) }
    }

    override fun trigger() {
        if (!isReleased()) property.trigger()
    }

    override val triggers: Flow<Unit> get() = flow
}
