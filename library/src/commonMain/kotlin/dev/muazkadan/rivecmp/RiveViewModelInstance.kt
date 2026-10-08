package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The view model instance bound to a [CustomRiveAnimation]. Properties are looked up by path, for
 * example `"rating"`, or `"card/title"` for a property of a nested view model.
 *
 * Every lookup returns `null` when the path does not exist or names a property of another type.
 * The instance and its properties are valid while the animation is in the composition and until
 * the binding is replaced. After that, lookups return `null` and writes have no effect.
 */
@ExperimentalRiveCmpApi
interface RiveViewModelInstance {
    fun number(path: String): RiveProperty<Float>?
    fun string(path: String): RiveProperty<String>?
    fun boolean(path: String): RiveProperty<Boolean>?

    /** A color as an ARGB integer. */
    fun color(path: String): RiveProperty<Int>?

    /** An enum as the name of its selected value. */
    fun enum(path: String): RiveProperty<String>?
    fun trigger(path: String): RiveTrigger?
}

/** A view model property holding a value of type [T]. */
@ExperimentalRiveCmpApi
interface RiveProperty<T> {
    /**
     * The current value. Writing it drives the graphic. Like setting a state machine input, a write
     * resumes playback that has stopped, including after [RiveComposition.pause], on Android, iOS
     * and desktop; on the web a paused animation stays paused.
     */
    var value: T

    /** The value over time, updated by the graphic and by writes to [value]. */
    val valueFlow: StateFlow<T>
}

/** A view model trigger property. */
@ExperimentalRiveCmpApi
interface RiveTrigger {
    /** Fires the trigger, resuming playback as a write to [RiveProperty.value] does. */
    fun trigger()

    /**
     * Emits each time the graphic fires the trigger. Whether a call to [trigger] is reported as
     * well depends on the platform's runtime, so do not rely on it to count your own calls.
     */
    val triggers: Flow<Unit>
}
