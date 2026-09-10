package dev.muazkadan.rivecmp.native

/** A boolean state machine input. */
public class SMIBoolean(unsafeCppPointer: Long) : SMIInput(unsafeCppPointer) {

    private external fun cppValue(cppPointer: Long): Boolean
    private external fun cppSetValue(cppPointer: Long, newValue: Boolean)

    public var value: Boolean
        get() = cppValue(cppPointer)
        internal set(value) = cppSetValue(cppPointer, value)

    override fun toString(): String = "SMIBoolean $name\n"
}
