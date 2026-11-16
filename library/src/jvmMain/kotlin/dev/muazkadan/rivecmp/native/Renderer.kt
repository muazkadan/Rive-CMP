package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.native.model.PixelFormat
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

    private external fun cppSetBuffer(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        rowBytes: Int,
        pixelFormatOrdinal: Int,
        rendererPointer: Long,
    )

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

    /**
     * Points the renderer at the pixels it draws into: [height] rows of [width] pixels in
     * [pixelFormat], each row starting [rowBytes] after the previous one. [buffer] must be a
     * direct buffer and must outlive the renderer's use of it.
     */
    public fun setBuffer(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        rowBytes: Int,
        pixelFormat: PixelFormat,
    ) {
        require(buffer.isDirect) { "The pixel buffer must be a direct buffer" }
        require(width > 0 && height > 0) { "The pixel buffer must not be empty: $width x $height" }
        require(rowBytes >= width * BYTES_PER_PIXEL) {
            "A row of $rowBytes bytes cannot hold $width pixels"
        }
        require(rowBytes % BYTES_PER_PIXEL == 0) {
            "A row of $rowBytes bytes is not a whole number of $BYTES_PER_PIXEL-byte pixels"
        }
        val requiredBytes = rowBytes.toLong() * (height - 1) + width.toLong() * BYTES_PER_PIXEL
        require(buffer.capacity() >= requiredBytes) {
            "A buffer of ${buffer.capacity()} bytes cannot hold $height rows of $rowBytes bytes"
        }
        cppSetBuffer(buffer, width, height, rowBytes, pixelFormat.ordinal, cppPointer)
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

    private companion object {
        const val BYTES_PER_PIXEL = 4
    }
}
