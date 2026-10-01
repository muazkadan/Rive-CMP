/**
 * iOS implementation of the view model instance API over rive-ios's data binding instance.
 * rive-ios reports changes through listeners, which feed the flows here. Each property is wrapped
 * once per path, so looking it up again registers no further listener.
 *
 * rive-ios properties point into their instance without retaining it, so every property here keeps
 * its adapter, and with it the instance, alive. Once the adapter is released with its animation,
 * lookups find nothing, reads return the last value seen and writes do nothing.
 */
@file:OptIn(ExperimentalForeignApi::class)

package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nativeIosShared.RiveDataBindingViewModelInstance
import platform.UIKit.UIColor
import kotlin.math.roundToInt

@OptIn(ExperimentalRiveCmpApi::class)
internal class IosRiveViewModelInstance(
    private val instance: RiveDataBindingViewModelInstance,
    private val onWrite: () -> Unit,
) : RiveViewModelInstance {

    private val releases = mutableListOf<() -> Unit>()
    private val adapters = mutableMapOf<String, Any>()
    private var released = false

    override fun number(path: String): RiveProperty<Float>? = cached("number", path) {
        instance.numberPropertyFromPath(path)?.let { property ->
            val flow = MutableStateFlow(property.value())
            val listener = property.addListener { flow.value = it }
            releases += { property.removeListener(listener) }
            Property(flow, read = { property.value() }, write = { property.setValue(it) })
        }
    }

    override fun string(path: String): RiveProperty<String>? = cached("string", path) {
        instance.stringPropertyFromPath(path)?.let { property ->
            val flow = MutableStateFlow(property.value())
            val listener = property.addListener { flow.value = it.orEmpty() }
            releases += { property.removeListener(listener) }
            Property(flow, read = { property.value() }, write = { property.setValue(it) })
        }
    }

    override fun boolean(path: String): RiveProperty<Boolean>? = cached("boolean", path) {
        instance.booleanPropertyFromPath(path)?.let { property ->
            val flow = MutableStateFlow(property.value())
            val listener = property.addListener { flow.value = it }
            releases += { property.removeListener(listener) }
            Property(flow, read = { property.value() }, write = { property.setValue(it) })
        }
    }

    override fun color(path: String): RiveProperty<Int>? = cached("color", path) {
        instance.colorPropertyFromPath(path)?.let { property ->
            val flow = MutableStateFlow(property.value().toArgb())
            val listener = property.addListener { color -> color?.let { flow.value = it.toArgb() } }
            releases += { property.removeListener(listener) }
            Property(
                flow,
                read = { property.value().toArgb() },
                write = { argb ->
                    property.setRed(
                        red = ((argb shr 16) and 0xFF) / 255.0,
                        green = ((argb shr 8) and 0xFF) / 255.0,
                        blue = (argb and 0xFF) / 255.0,
                        alpha = ((argb shr 24) and 0xFF) / 255.0,
                    )
                },
            )
        }
    }

    override fun enum(path: String): RiveProperty<String>? = cached("enum", path) {
        instance.enumPropertyFromPath(path)?.let { property ->
            val flow = MutableStateFlow(property.value())
            val listener = property.addListener { flow.value = it.orEmpty() }
            releases += { property.removeListener(listener) }
            Property(flow, read = { property.value() }, write = { property.setValue(it) })
        }
    }

    override fun trigger(path: String): RiveTrigger? = cached("trigger", path) {
        instance.triggerPropertyFromPath(path)?.let { property ->
            val flow = MutableSharedFlow<Unit>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            val listener = property.addListener { flow.tryEmit(Unit) }
            releases += { property.removeListener(listener) }
            Trigger(flow, fire = { property.trigger() })
        }
    }

    /** The adapter already made for this kind of property at [path], or a new one from [create]. */
    @Suppress("UNCHECKED_CAST")
    private fun <A : Any> cached(kind: String, path: String, create: () -> A?): A? {
        if (released) return null
        val key = "$kind:$path"
        return (adapters[key] as A?) ?: create()?.also { adapters[key] = it }
    }

    /** Removes the listeners registered on rive-ios's properties and stops touching them. */
    fun release() {
        releases.forEach { it() }
        releases.clear()
        adapters.clear()
        released = true
    }

    private inner class Property<T>(
        private val flow: MutableStateFlow<T>,
        private val read: () -> T,
        private val write: (T) -> Unit,
    ) : RiveProperty<T> {
        override var value: T
            get() = if (released) flow.value else read()
            set(value) {
                if (released) return
                write(value)
                flow.value = value
                onWrite()
            }

        override val valueFlow: StateFlow<T> get() = flow
    }

    private inner class Trigger(
        private val flow: MutableSharedFlow<Unit>,
        private val fire: () -> Unit,
    ) : RiveTrigger {
        override fun trigger() {
            if (released) return
            fire()
            onWrite()
        }

        override val triggers: Flow<Unit> get() = flow
    }
}

/** This color as an ARGB integer. */
private fun UIColor.toArgb(): Int = memScoped {
    val red = alloc<DoubleVar>()
    val green = alloc<DoubleVar>()
    val blue = alloc<DoubleVar>()
    val alpha = alloc<DoubleVar>()
    getRed(red.ptr, green.ptr, blue.ptr, alpha.ptr)
    fun channel(component: Double) = (component * 255).roundToInt().coerceIn(0, 255)
    (channel(alpha.value) shl 24) or (channel(red.value) shl 16) or (channel(green.value) shl 8) or channel(blue.value)
}
