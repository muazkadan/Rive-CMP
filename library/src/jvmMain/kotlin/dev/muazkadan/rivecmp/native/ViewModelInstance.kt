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

    private external fun cppPropertyNumber(cppPointer: Long, path: String): Long
    private external fun cppPropertyString(cppPointer: Long, path: String): Long
    private external fun cppPropertyBoolean(cppPointer: Long, path: String): Long
    private external fun cppPropertyColor(cppPointer: Long, path: String): Long
    private external fun cppPropertyEnum(cppPointer: Long, path: String): Long
    private external fun cppPropertyTrigger(cppPointer: Long, path: String): Long

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
     * Get the number property at [path], a name or a `/`-separated path into nested view models.
     *
     * @throws ViewModelException If there is no number property at [path].
     */
    @Throws(ViewModelException::class)
    public fun getNumberProperty(path: String): ViewModelNumberProperty =
        getProperty(path, ::cppPropertyNumber, ::ViewModelNumberProperty)

    /** @throws ViewModelException If there is no string property at [path]. */
    @Throws(ViewModelException::class)
    public fun getStringProperty(path: String): ViewModelStringProperty =
        getProperty(path, ::cppPropertyString, ::ViewModelStringProperty)

    /** @throws ViewModelException If there is no boolean property at [path]. */
    @Throws(ViewModelException::class)
    public fun getBooleanProperty(path: String): ViewModelBooleanProperty =
        getProperty(path, ::cppPropertyBoolean, ::ViewModelBooleanProperty)

    /** @throws ViewModelException If there is no color property at [path]. */
    @Throws(ViewModelException::class)
    public fun getColorProperty(path: String): ViewModelColorProperty =
        getProperty(path, ::cppPropertyColor, ::ViewModelColorProperty)

    /** @throws ViewModelException If there is no enum property at [path]. */
    @Throws(ViewModelException::class)
    public fun getEnumProperty(path: String): ViewModelEnumProperty =
        getProperty(path, ::cppPropertyEnum, ::ViewModelEnumProperty)

    /** @throws ViewModelException If there is no trigger property at [path]. */
    @Throws(ViewModelException::class)
    public fun getTriggerProperty(path: String): ViewModelTriggerProperty =
        getProperty(path, ::cppPropertyTrigger, ::ViewModelTriggerProperty)

    /** Returns the property at [path], creating and remembering it so [pollChanges] reaches it. */
    private inline fun <reified P : ViewModelProperty<*>> getProperty(
        path: String,
        lookup: (Long, String) -> Long,
        create: (Long) -> P,
    ): P {
        (properties[path] as? P)?.let { return it }
        val pointer = lookup(cppPointer, path)
        if (pointer == NULL_POINTER)
            throw ViewModelException("No such property \"$path\" on view model instance \"$name\".")
        return create(pointer).also { properties[path] = it }
    }

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

/** A number property of a [ViewModelInstance]. */
public class ViewModelNumberProperty internal constructor(unsafeCppPointer: Long) :
    ViewModelProperty<Float>(unsafeCppPointer) {
    private external fun cppGetValue(cppPointer: Long): Float
    private external fun cppSetValue(cppPointer: Long, value: Float)

    override fun nativeGetValue(): Float = cppGetValue(cppPointer)
    override fun nativeSetValue(value: Float): Unit = cppSetValue(cppPointer, value)
}

/** A string property of a [ViewModelInstance]. */
public class ViewModelStringProperty internal constructor(unsafeCppPointer: Long) :
    ViewModelProperty<String>(unsafeCppPointer) {
    private external fun cppGetValue(cppPointer: Long): String
    private external fun cppSetValue(cppPointer: Long, value: String)

    override fun nativeGetValue(): String = cppGetValue(cppPointer)
    override fun nativeSetValue(value: String): Unit = cppSetValue(cppPointer, value)
}

/** A boolean property of a [ViewModelInstance]. */
public class ViewModelBooleanProperty internal constructor(unsafeCppPointer: Long) :
    ViewModelProperty<Boolean>(unsafeCppPointer) {
    private external fun cppGetValue(cppPointer: Long): Boolean
    private external fun cppSetValue(cppPointer: Long, value: Boolean)

    override fun nativeGetValue(): Boolean = cppGetValue(cppPointer)
    override fun nativeSetValue(value: Boolean): Unit = cppSetValue(cppPointer, value)
}

/** A color property of a [ViewModelInstance], as an ARGB integer. */
public class ViewModelColorProperty internal constructor(unsafeCppPointer: Long) :
    ViewModelProperty<Int>(unsafeCppPointer) {
    private external fun cppGetValue(cppPointer: Long): Int
    private external fun cppSetValue(cppPointer: Long, value: Int)

    override fun nativeGetValue(): Int = cppGetValue(cppPointer)
    override fun nativeSetValue(value: Int): Unit = cppSetValue(cppPointer, value)
}

/** An enum property of a [ViewModelInstance], as the name of its selected value. */
public class ViewModelEnumProperty internal constructor(unsafeCppPointer: Long) :
    ViewModelProperty<String>(unsafeCppPointer) {
    private external fun cppGetValue(cppPointer: Long): String
    private external fun cppSetValue(cppPointer: Long, value: String)

    override fun nativeGetValue(): String = cppGetValue(cppPointer)
    override fun nativeSetValue(value: String): Unit = cppSetValue(cppPointer, value)
}

/**
 * A trigger property of a [ViewModelInstance]. It has no value: [valueFlow] holds a new
 * [TriggerUnit] each time the trigger fires.
 */
public class ViewModelTriggerProperty internal constructor(unsafeCppPointer: Long) :
    ViewModelProperty<ViewModelTriggerProperty.TriggerUnit>(unsafeCppPointer) {

    /** Marks one firing of a trigger. Instances are distinct so each firing is a new flow value. */
    public class TriggerUnit

    private external fun cppTrigger(cppPointer: Long)

    override fun nativeGetValue(): TriggerUnit = TriggerUnit()
    override fun nativeSetValue(value: TriggerUnit): Unit = Unit

    /** Fires the trigger. */
    public fun trigger(): Unit = cppTrigger(cppPointer)
}
