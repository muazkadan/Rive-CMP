package dev.muazkadan.rivecmp

import app.rive.runtime.kotlin.RiveAnimationView
import java.lang.ref.WeakReference

actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var animationViewRef: RiveAnimationView? = null

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        animationViewRef?.setNumberState(
            stateMachineName = stateMachineName,
            inputName = name,
            value = value
        )
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        animationViewRef?.setBooleanState(
            stateMachineName = stateMachineName,
            inputName = name,
            value = value
        )
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        animationViewRef?.fireState(stateMachineName = stateMachineName, inputName = name)
    }

    actual fun pause() {
        animationViewRef?.pause()
    }

    actual fun reset() {
        animationViewRef?.reset()
    }

    actual fun stop() {
        animationViewRef?.stop()
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        animationViewRef = animationView as? RiveAnimationView
    }
} 