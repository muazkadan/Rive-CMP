package dev.muazkadan.rivecmp.native.model

/**
 * The byte layout of the 32-bit, premultiplied-alpha pixels in a buffer handed to a
 * [Renderer][dev.muazkadan.rivecmp.native.Renderer]. The native bridge draws in the order it is
 * told here, so the owner of the buffer states the order it allocated instead of both sides
 * assuming one.
 *
 * The native side reads these by ordinal, so the order of the entries must not change.
 */
public enum class PixelFormat {

    /** Red, green, blue, alpha, one byte each in that order in memory. */
    RGBA_8888,

    /** Blue, green, red, alpha, one byte each in that order in memory. */
    BGRA_8888,
}
