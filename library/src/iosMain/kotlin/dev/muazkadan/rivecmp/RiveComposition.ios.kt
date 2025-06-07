package dev.muazkadan.rivecmp

import kotlinx.cinterop.ExperimentalForeignApi
import nativeIosShared.RiveAnimationController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var controllerRef: RiveAnimationController? = null

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        controllerRef?.setNumberInput(name, value)
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        controllerRef?.setBooleanInput(name, value)
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        controllerRef?.setTriggerInput(name)
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        controllerRef = animationView as? RiveAnimationController
    }
} 