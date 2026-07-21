package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.native.NativeObject.Companion.NULL_POINTER
import java.util.Collections
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * NativeObject is a Kotlin object that's backed by a C++ counterpart via the JNI. It keeps track of
 * the current pointer value in its local variable [unsafeCppPointer].
 *
 * [unsafeCppPointer] is accessible via the [cppPointer] getter/setter.
 */
public abstract class NativeObject(initialPointer: Long) : RefCount {

    private val unsafeCppPointer = AtomicLong(initialPointer)

    /**
     * A cache of the stack trace at the time that this object was disposed. Appended to the stack
     * trace to diagnose when attempting to use this object after it has been disposed.
     */
    private var disposeStackTrace: Sequence<StackTraceElement>? = null

    public companion object {

        /** Static const value for an empty pointer. */
        public const val NULL_POINTER: Long = 0L
    }

    /** Whether this objects underlying pointer is still valid. */
    public val hasCppObject: Boolean get() = unsafeCppPointer.load() != NULL_POINTER

    final override var refs: AtomicInt = AtomicInt(
        if (initialPointer == NULL_POINTER) 0 // null objects cannot be referenced.
        else 1,
    )

    /**
     * Getter/Setter for the underlying C++ pointer value.
     *
     * @throws Exception if this object wraps a [unsafeCppPointer] set to [NULL_POINTER].
     */
    public var cppPointer: Long
        set(value) = unsafeCppPointer.store(value)
        @Throws(Exception::class)
        get() {
            val pointer = unsafeCppPointer.load()
            if (pointer == NULL_POINTER) {
                val nativeObjectName = this.javaClass.simpleName
                val riveException = Exception(
                    "Accessing disposed C++ object $nativeObjectName.",
                )

                riveException.stackTrace = buildCombinedStackTrace().toTypedArray()

                throw riveException
            }
            return pointer
        }

    // Collection of native objects that are owned(created) by this.
    public val dependencies: MutableList<RefCount> =
        Collections.synchronizedList(mutableListOf<RefCount>())

    // Up to the implementer (interfaces cannot have external functions)
    public open fun cppDelete(pointer: Long): Unit = Unit

    /**
     * Builds a combined stack trace incorporating the disposal stack trace and the current access
     * trace. This helps diagnosing issues with invalid memory access.
     *
     * @return A list of [StackTraceElement] combining `dispose()` Stack Trace (if available) with
     *    the current one.
     */
    private fun buildCombinedStackTrace(): List<StackTraceElement> {
        val combinedTrace = mutableListOf<StackTraceElement>()

        // Append the disposal stack trace if available
        disposeStackTrace?.also { trace ->
            combinedTrace += StackTraceElement(
                "--- Stack Trace for NativeObject Dispose ---",
                "", null, -1,
            )
            combinedTrace += trace
            combinedTrace += StackTraceElement("--- Current Stack Trace ---", "", null, -1)
        }

        // Append the current stack trace
        combinedTrace += Thread.currentThread().stackTrace.asSequence()
            .dropWhile { it.className != NativeObject::class.java.name } // Drop system methods
            .drop(1) // Drop buildCombinedStackTrace()

        return combinedTrace
    }

    /**
     * Increments the references for this counter. Cannot be used for initialization - use
     * [refs].[incrementAndFetch][AtomicInt.incrementAndFetch] instead.
     *
     * @return The new reference count.
     * @throws IllegalStateException if refs already is 0.
     */
    @Throws(IllegalArgumentException::class)
    @Synchronized
    override fun acquire(): Int {
        val count = super.acquire()
        require(count > 1) // Never acquire a disposed object.
        return count
    }

    /**
     * Decrements the reference counter.
     *
     * @return The new reference count.
     * @throws IllegalStateException if [refs] is already 0.
     */
    @Throws(IllegalArgumentException::class)
    @Synchronized
    override fun release(): Int {
        val count = super.release()
        require(count >= 0) // Never release a disposed object.

        if (count == 0 && hasCppObject) dispose()

        return count
    }

    /**
     * Disposes of this reference and potentially any of the dependents.
     *
     * Uses [refs] to keep track of how many objects are using this NativeObject. When refs == 0 the
     * object will be disposed.
     *
     * @throws IllegalStateException if [refs] is not 0.
     */
    @Throws(IllegalArgumentException::class)
    @Synchronized
    private fun dispose() {
        require(refs.load() == 0)

        disposeStackTrace =
            Thread.currentThread().stackTrace.asSequence()
                .dropWhile { it.className != NativeObject::class.java.name } // Drop system methods

        dependencies.apply {
            // Release all dependencies and clear the collection.
            // If anyone is holding a reference to one of this object's dependents, they will need
            // to also `release()` it to clean up memory.
            forEach(RefCount::release)
//            clear()
        }
        cppDelete(unsafeCppPointer.load())
        unsafeCppPointer.store(NULL_POINTER)
    }
}
