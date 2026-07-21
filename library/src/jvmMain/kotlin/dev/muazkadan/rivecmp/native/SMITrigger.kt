package dev.muazkadan.rivecmp.native

/** A trigger state machines input. */
public class SMITrigger(unsafeCppPointer: Long) : SMIInput(unsafeCppPointer) {

    private external fun cppFire(cppPointer: Long)

    public fun fire(): Unit = cppFire(cppPointer)

    override fun toString(): String = "SMITrigger $name\n"
}
