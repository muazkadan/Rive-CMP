package dev.muazkadan.rivecmp.native

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents an instantiated set of properties on a ViewModel. With this class you have access to
 * the individual properties to get and set values from bindings.
 *
 * Before the property modifications have any effect, you need to assign the instance to an artboard
 * with [Artboard.viewModelInstance].
 *
 * Instances can be created from one [File] and applied to another.
 *
 * @param unsafeCppPointer Pointer to the C++ counterpart.
 */
public class ViewModelInstance internal constructor(unsafeCppPointer: Long) :
    NativeObject(unsafeCppPointer) {

    private external fun cppName(cppPointer: Long): String

    private external fun cppRef(cppPointer: Long)

    private var properties: MutableMap<String, ViewModelProperty<*>> = ConcurrentHashMap()
    private var children: MutableMap<String, ViewModelInstance> = ConcurrentHashMap()

    init {
        // Keep an extra reference to the instance. Cleaned up in cppDelete.
        // This is to facilitate the transfer use case where a user holds an instance after its
        // originating file has been deleted.
        cppRef(cppPointer)
    }

    // Un-ref the extra reference created in init.
    external override fun cppDelete(pointer: Long)

    /** Get the [name] of the view model instance. */
    public val name: String
        get() = cppName(cppPointer)

    /**
     * Poll all properties for changes. This is called from advance.
     */
    internal fun pollChanges() {
        properties.values.forEach(ViewModelProperty<*>::pollChanges)
        children.values.forEach(ViewModelInstance::pollChanges)
    }
}

/**
 * A property of type [T] of a [ViewModelInstance]. Use [value] to mutate the property. use
 * [valueFlow] to subscribe to changes on the value.
 */
public abstract class ViewModelProperty<T>(unsafeCppPointer: Long) :
    NativeObject(unsafeCppPointer) {

    private external fun cppName(cppPointer: Long): String

    private external fun cppHasChanged(cppPointer: Long): Boolean
    private external fun cppFlushChanges(cppPointer: Long): Boolean

    /** The name of the property. */
    public val name: String
        get() = cppName(cppPointer)

    /**
     * The current value of the property, whether set by this property or by a data binding update.
     */
    public var value: T
        get() = valueFlow.value
        set(value) {
            nativeSetValue(value)
            mutableValueFlow.value = value
        }

    protected abstract fun nativeGetValue(): T
    protected abstract fun nativeSetValue(value: T)

    private val mutableValueFlow: MutableStateFlow<T> = MutableStateFlow(nativeGetValue())

    /** A flow of the property's value. Use for observing changes. */
    public val valueFlow: StateFlow<T> get() = mutableValueFlow

    internal fun pollChanges() {
        if (cppHasChanged(cppPointer)) {
            mutableValueFlow.value = nativeGetValue()
            cppFlushChanges(cppPointer)
        }
    }
}
