/**
 * JS implementation of the view model instance API over @rive-app/canvas's ViewModelInstance.
 * The runtime reports changes through callbacks, which feed the flows here. Each property is wrapped
 * once per path, so looking it up again registers no further callback. Once released, lookups find
 * nothing, reads return the last value seen and writes do nothing.
 */
package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalRiveCmpApi::class)
internal class JsRiveViewModelInstance(private val instance: dynamic) : RiveViewModelInstance {

    private val observed = mutableListOf<dynamic>()
    private val adapters = mutableMapOf<String, Any>()
    private var released = false

    override fun number(path: String): RiveProperty<Float>? = cached("number", path) {
        property(instance.number(path), read = { (it as Number).toFloat() }, write = { it })
    }

    override fun string(path: String): RiveProperty<String>? = cached("string", path) {
        property(instance.string(path), read = { it as String }, write = { it })
    }

    override fun boolean(path: String): RiveProperty<Boolean>? = cached("boolean", path) {
        property(instance.boolean(path), read = { it as Boolean }, write = { it })
    }

    override fun color(path: String): RiveProperty<Int>? = cached("color", path) {
        property(instance.color(path), read = { (it as Number).toInt() }, write = { it })
    }

    override fun enum(path: String): RiveProperty<String>? = cached("enum", path) {
        property(instance.enum(path), read = { it as String }, write = { it })
    }

    override fun trigger(path: String): RiveTrigger? = cached("trigger", path) {
        val property = instance.trigger(path)
        if (property == null) null else JsRiveTrigger(property, isReleased = { released }).also { observed.add(property) }
    }

    private fun <T> property(property: dynamic, read: (dynamic) -> T, write: (T) -> dynamic): RiveProperty<T>? {
        if (property == null) return null
        observed.add(property)
        return JsRiveProperty(property, read, write, isReleased = { released })
    }

    /** The adapter already made for this kind of property at [path], or a new one from [create]. */
    @Suppress("UNCHECKED_CAST")
    private fun <A : Any> cached(kind: String, path: String, create: () -> A?): A? {
        if (released) return null
        val key = "$kind:$path"
        return (adapters[key] as A?) ?: create()?.also { adapters[key] = it }
    }

    /** Removes the callbacks registered on the runtime's properties and stops touching them. */
    fun release() {
        observed.forEach { it.off() }
        observed.clear()
        adapters.clear()
        released = true
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
private class JsRiveProperty<T>(
    private val property: dynamic,
    private val read: (dynamic) -> T,
    private val write: (T) -> dynamic,
    private val isReleased: () -> Boolean,
) : RiveProperty<T> {
    private val flow = MutableStateFlow(read(property.value))

    init {
        property.on { newValue: dynamic -> flow.value = read(newValue) }
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
private class JsRiveTrigger(
    private val property: dynamic,
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
