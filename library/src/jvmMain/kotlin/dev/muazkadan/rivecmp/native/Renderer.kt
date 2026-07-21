package dev.muazkadan.rivecmp.native

import java.nio.ByteBuffer
import kotlin.concurrent.atomics.incrementAndFetch

public abstract class Renderer : NativeObject(NULL_POINTER) {

    init {
        cppPointer = constructor()
        refs.incrementAndFetch()
        assert(cppPointer != NULL_POINTER)
    }

    // From NativeObject
    external override fun cppDelete(pointer: Long)

    private external fun cppSetBuffer(buffer: ByteBuffer, width: Int, height: Int, rendererPointer: Long)

    /** Instantiates JNIRenderer in C++ */
    private external fun constructor(): Long

    public var isPlaying: Boolean = false
        private set
    private var isAttached: Boolean = false

    public abstract fun draw()

    public abstract fun advance(elapsed: Float)

    public fun doFrame(elapsed: Float) {
        if (!isPlaying) return
        if (!hasCppObject) return
        advance(elapsed)
        draw()
    }

    /**
     * Starts the renderer and registers for frameCallbacks.
     *
     */
    public fun start() {
        if (isPlaying) return
        if (!isAttached) return
        if (!hasCppObject) return
        isPlaying = true
    }

    public fun setBuffer(buffer: ByteBuffer, width: Int, height: Int) {
        cppSetBuffer(buffer, width, height, cppPointer)
        isAttached = true
        start()
    }

    /**
     * Marks the animation as stopped.
     *
     * Lets the underlying renderer know we are intending to stop animating.
     *
     *
     */
    internal fun stop() {
        if (!isPlaying) return
        if (!hasCppObject) return
        // Prevent any other frame to be scheduled.
        isPlaying = false
    }

    /**
     * The deletion of the underlying C++ object.
     *
     */
    override fun release(): Int {
        stop()
        isAttached = false
        return super.release()
    }
}
