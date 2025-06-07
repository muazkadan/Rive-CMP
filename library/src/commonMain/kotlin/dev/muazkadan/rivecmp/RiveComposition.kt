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

class RiveComposition internal constructor(
    internal val spec: RiveCompositionSpec
)
