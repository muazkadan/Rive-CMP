package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var riveInstance: RiveSDK.Rive? = null

    /** Whether [reset] binds a view model instance again, as the view was created to do. */
    internal var autoBind: Boolean = false

    /** Runs after [reset], which replaces the bound view model instance. */
    internal var afterReset: (() -> Unit)? = null

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        val inputs = riveInstance?.stateMachineInputs(stateMachineName)
        if (inputs != null) {
            val input = (inputs as Array<dynamic>).find { it.name == name }
            if (input != null) {
                input.value = value
            }
        }
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        val inputs = riveInstance?.stateMachineInputs(stateMachineName)
        if (inputs != null) {
            val input = (inputs as Array<dynamic>).find { it.name == name }
            if (input != null) {
                input.value = value
            }
        }
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        val inputs = riveInstance?.stateMachineInputs(stateMachineName)
        if (inputs != null) {
            val input = (inputs as Array<dynamic>).find { it.name == name }
            if (input != null) {
                input.fire()
            }
        }
    }

    actual fun pause() {
        riveInstance?.pause()
    }

    actual fun reset() {
        // Rive Web SDK reset() takes an options object (RiveResetParameters)
        // Pass empty object to reset everything to initial state without forcing autoplay
        // This matches the behavior of Android and iOS implementations which preserve the original autoplay state
        val params = js("{}")
        params.autoBind = autoBind
        riveInstance?.reset(params)
        afterReset?.invoke()
    }

    actual fun stop() {
        riveInstance?.stop()
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        riveInstance = animationView as? RiveSDK.Rive
    }
}
