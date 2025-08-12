package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.external.RiveClass

actual class RiveComposition internal actual constructor(spec: RiveCompositionSpec) {
    internal actual val spec: RiveCompositionSpec = spec

    // Reference to the underlying Rive runtime instance created by the Composable
    private var riveInstance: RiveClass? = null

    actual fun setNumberInput(
        stateMachineName: String,
        name: String,
        value: Float
    ) {
        // Not supported by current JS interop (@rive-app/canvas wrapper exposed here)
        // Left as no-op for web target.
    }

    actual fun setBooleanInput(
        stateMachineName: String,
        name: String,
        value: Boolean
    ) {
        // Not supported by current JS interop; no-op.
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        // Not supported by current JS interop; no-op.
    }

    actual fun pause() {
        riveInstance?.pause()
    }

    actual fun reset() {
        // Minimal behavior: pause the animation. Advanced reset would require
        // reloading the composition which is not covered by current interop.
        riveInstance?.pause()
    }

    actual fun stop() {
        // Cleanup underlying instance if available
        riveInstance?.cleanup()
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        riveInstance = animationView as? RiveClass
    }
}