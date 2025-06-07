package dev.muazkadan.rivecmp;

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

@Composable
fun rememberRiveComposition(
    vararg keys: Any?,
    spec: suspend () -> RiveCompositionSpec,
): RiveCompositionResult {

    val result = remember(*keys) {
        RiveCompositionResultImpl()
    }

    LaunchedEffect(result) {
        try {
            val composition = withContext(Dispatchers.IO) {
                val specInstance = spec()
                specInstance.load()
            }
            result.complete(composition)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            result.completeWithError(t)
        }
    }

    return result
}

expect class RiveComposition internal constructor(
    spec: RiveCompositionSpec
) {
    internal val spec: RiveCompositionSpec

    fun setNumberInput(stateMachineName: String, name: String, value: Float)
    fun setBooleanInput(stateMachineName: String, name: String, value: Boolean)
    fun setTriggerInput(stateMachineName: String, name: String)
    fun pause()
    fun reset()
    fun stop()

    internal fun connectToAnimationView(animationView: Any?)
}
