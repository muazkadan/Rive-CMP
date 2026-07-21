package dev.muazkadan.rivecmp.native

/** A floating point number state machine input. */
public class SMINumber(unsafeCppPointer: Long) : SMIInput(unsafeCppPointer) {

    private external fun cppValue(cppPointer: Long): Float
    private external fun cppSetValue(cppPointer: Long, value: Float)

    public var value: Float
        get() = cppValue(cppPointer)
        internal set(value) = cppSetValue(cppPointer, value)

    override fun toString(): String = "SMINumber $name\n"
}
