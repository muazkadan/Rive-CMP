package dev.muazkadan.rivecmp.native


/**
 * A description of a ViewModel in the Rive file. It can be used to retrieve property definitions
 * at runtime. However, properties cannot be modified - that requires an instance. Use one of
 * the createInstance methods to create an instance with mutable properties from this ViewModel.
 *
 * @param unsafeCppPointer Pointer to the C++ counterpart.
 */
public class ViewModel internal constructor(unsafeCppPointer: Long) : NativeObject(unsafeCppPointer) {

    private external fun cppName(cppPointer: Long): String

    private external fun cppCreateDefaultInstance(cppPointer: Long): Long

    /** Get the [name] of the ViewModel. */
    public val name: String
        get() = cppName(cppPointer)

    /**
     * Create a new ViewModelInstance. Use Artboard::setViewModel to apply it.
     *
     * Property values will be those of the instance marked as "Default" in the Rive editor.
     *
     * @return A new instance of the ViewModel with initial values supplied by the default instance.
     * @throws ViewModelException If the default instance cannot be created.
     */
    public fun createDefaultInstance(): ViewModelInstance {
        val instancePointer = cppCreateDefaultInstance(cppPointer)
        if (instancePointer == NULL_POINTER)
            throw ViewModelException("Could not create default ViewModel instance")

        return ViewModelInstance(instancePointer).also(dependencies::add)
    }
}
