package dev.muazkadan.rivecmp.native

import kotlin.Throws

/**
 * The [LinearAnimationInstance] is a helper to wrap common operations to play an animation.
 *
 * Use this to keep track of an animation's current state and progress. You may also [apply] changes
 * that the animation makes to components in an [Artboard].
 *
 * @param unsafeCppPointer Pointer to the C++ counterpart.
 */
public class LinearAnimationInstance(
    unsafeCppPointer: Long,
    public var mix: Float = 1.0f,
) : PlayableInstance, NativeObject(unsafeCppPointer) {

    private external fun cppAdvanceAndGetResult(pointer: Long, elapsedTime: Float): AdvanceResult
    private external fun cppApply(pointer: Long, mix: Float)
    private external fun cppSetTime(pointer: Long, time: Float)
    private external fun cppGetDirection(pointer: Long): Int
    private external fun cppSetDirection(pointer: Long, int: Int)
    private external fun cppGetLoop(cppPointer: Long): Int
    private external fun cppSetLoop(cppPointer: Long, value: Int)
    private external fun cppName(cppPointer: Long): String
    private external fun cppDuration(cppPointer: Long): Int
    private external fun cppFps(cppPointer: Long): Int
    private external fun cppWorkEnd(cppPointer: Long): Int

    external override fun cppDelete(pointer: Long)

    /**
     * Advance the animation and return the result.
     *
     * @param elapsedTime The time in seconds to advance by.
     * @return An [AdvanceResult] enum value indicating the outcome of the advance step
     */
    public fun advanceAndGetResult(elapsedTime: Float): AdvanceResult =
        cppAdvanceAndGetResult(cppPointer, elapsedTime)

    /**
     * Applies the animation instance's current set of transformations to an [Artboard].
     *
     * Uses the [mix] property (a value between 0 and 1) to set the strength at which the animation
     * is mixed with other animations applied to the [Artboard].
     */
    public fun apply(): Unit = cppApply(cppPointer, mix)

    /** Seeks the animation to [time]. */
    public fun time(time: Float): Unit = cppSetTime(cppPointer, time)

    /**
     * Configure the animation to play [forwards][Direction.FORWARDS] or
     * [backwards][Direction.BACKWARDS].
     */
    public var direction: Direction
        @Throws(IllegalStateException::class)
        get() = checkNotNull(Direction.fromInt(cppGetDirection(cppPointer)))
        set(direction) = cppSetDirection(cppPointer, direction.value)

    /**
     * The duration of an animation in frames.
     */
    public val duration: Int
        get() = cppDuration(cppPointer)

    /** Return the frames per second (FPS) configured for this animation. */
    public val fps: Int
        get() = cppFps(cppPointer)

    /**
     * The offset in frames to the end of an animations work area. Animations will execute their
     * loop behavior once this is reached.
     */
    public val workEnd: Int
        get() = cppWorkEnd(cppPointer)

    /** The name given to this animation. */
    override val name: String
        get() = cppName(cppPointer)

    /** The offset in seconds to the end of the animation. */
    public val endTime: Float
        get() = if (workEnd == -1) duration.toFloat() / fps else workEnd.toFloat() / fps

    /**
     * Configure the [Loop] mode for this animation. Can be either [Loop.LOOP], [Loop.ONESHOT] or [Loop.PINGPONG].
     */
    public var loop: Loop
        get() {
            val intLoop = cppGetLoop(cppPointer)
            return Loop.fromIndex(intLoop)
        }
        set(loop) = cppSetLoop(cppPointer, loop.ordinal)
}
