package dev.muazkadan.rivecmp

import app.rive.ViewModelInstance

actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var viewModelInstanceRef: ViewModelInstance? = null

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        viewModelInstanceRef?.setNumber(name, value)
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        viewModelInstanceRef?.setBoolean(name, value)
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        viewModelInstanceRef?.fireTrigger(name)
    }

    actual fun pause() {
        // Pausing is no longer supported directly on the ViewModelInstance.
        // It's handled at the Composable level. This method might be a no-op 
        // or require architecture changes if pause logic needs to be retained here.
    }

    actual fun reset() {
        // Handled via state machine reloads or data binds in the new API
    }

    actual fun stop() {
        // Stop is equivalent to pausing or detaching in the new API
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        viewModelInstanceRef = animationView as? ViewModelInstance
    }
} 