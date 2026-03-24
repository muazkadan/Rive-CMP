@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var riveInstance: Rive? = null

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        setNumberInputValue(riveInstance, stateMachineName, name, value)
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        setBooleanInputValue(riveInstance, stateMachineName, name, value)
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        fireInput(riveInstance, stateMachineName, name)
    }

    actual fun pause() {
        riveInstance?.pause()
    }

    actual fun reset() {
        riveInstance?.reset(emptyResetOptions())
    }

    actual fun stop() {
        riveInstance?.stop()
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        riveInstance = animationView as? Rive
    }
}

private fun setNumberInputValue(
    instance: Rive?,
    stateMachineName: String,
    name: String,
    value: Float
): Unit = js(
    """
    {
        if (!instance) return;
        const inputs = instance.stateMachineInputs(stateMachineName);
        if (!inputs) return;
        const input = inputs.find((item) => item.name === name);
        if (input) {
            input.value = value;
        }
    }
    """
)

private fun setBooleanInputValue(
    instance: Rive?,
    stateMachineName: String,
    name: String,
    value: Boolean
): Unit = js(
    """
    {
        if (!instance) return;
        const inputs = instance.stateMachineInputs(stateMachineName);
        if (!inputs) return;
        const input = inputs.find((item) => item.name === name);
        if (input) {
            input.value = value;
        }
    }
    """
)

private fun fireInput(
    instance: Rive?,
    stateMachineName: String,
    name: String
): Unit = js(
    """
    {
        if (!instance) return;
        const inputs = instance.stateMachineInputs(stateMachineName);
        if (!inputs) return;
        const input = inputs.find((item) => item.name === name);
        if (input) {
            input.fire();
        }
    }
    """
)
