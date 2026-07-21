package dev.muazkadan.rivecmp.native.model

/**
 * Defines how Rive animations should be fitted within their container.
 * This enum provides a unified API across Android and iOS platforms.
 */
public enum class Fit {

    /**
     * Scale the animation to fill the entire container, potentially cropping content.
     */
    FILL,

    /**
     * Scale the animation to fit within the container, maintaining aspect ratio.
     * This ensures the entire animation is visible within the bounds.
     */
    CONTAIN,

    /**
     * Scale the animation to cover the entire container, maintaining aspect ratio.
     * This may crop parts of the animation to fill the container.
     */
    COVER,

    /**
     * Scale the animation to fit the width of the container.
     */
    FIT_WIDTH,

    /**
     * Scale the animation to fit the height of the container.
     */
    FIT_HEIGHT,

    LAYOUT,

    /**
     * Do not scale the animation, use its original size.
     */
    NONE
}
