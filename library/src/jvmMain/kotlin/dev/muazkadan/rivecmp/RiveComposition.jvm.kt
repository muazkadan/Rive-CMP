package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.native.File
import dev.muazkadan.rivecmp.native.RiveFileController
import java.net.URI

actual class RiveComposition internal actual constructor(
    internal actual val spec: RiveCompositionSpec
) {

    // Runs on `defaultAsyncDispatcher` (IO) - `spec.load()` is only ever invoked from
    // `rememberRiveComposition`'s `withContext(defaultAsyncDispatcher) { spec().load() }`, so
    // blocking here to read bytes synchronously is safe and keeps `RiveComposition` ready to use
    // (with its native `File` already imported) as soon as construction returns.
    internal val file: File = when (val spec = spec) {
        is RiveUrlCompositionSpec -> File(URI(spec.url).toURL().readBytes())
        is RiveByteArrayCompositionSpec -> File(spec.byteArray)
        else -> throw IllegalArgumentException("Unsupported composition spec '$spec'")
    }

    private var controller: RiveFileController? = null

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        controller?.setNumberState(
            stateMachineName = stateMachineName,
            inputName = name,
            value = value,
        )
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        controller?.setBooleanState(
            stateMachineName = stateMachineName,
            inputName = name,
            value = value,
        )
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        controller?.fireState(stateMachineName = stateMachineName, inputName = name)
    }

    actual fun pause() {
        controller?.pause()
    }

    actual fun reset() {
        controller?.artboardRenderer?.reset()
    }

    actual fun stop() {
        controller?.stopAnimations()
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        controller = animationView as? RiveFileController
    }
}
