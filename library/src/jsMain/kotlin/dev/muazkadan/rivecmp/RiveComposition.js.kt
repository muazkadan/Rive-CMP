package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var riveInstance: RiveSDK.Rive? = null

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
        riveInstance?.reset(js("{}"))
    }

    actual fun stop() {
        riveInstance?.stop()
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        riveInstance = animationView as? RiveSDK.Rive
    }
}
