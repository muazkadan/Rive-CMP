package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
@Composable
fun CustomRiveAnimation(
    composition: RiveComposition?,
    modifier: Modifier = Modifier,
    alignment: RiveAlignment = RiveAlignment.CENTER,
    autoPlay: Boolean = true,
    artboardName: String? = null,
    fit: RiveFit = RiveFit.CONTAIN,
    stateMachineName: String? = null,
) {
    if (composition != null) {
        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                CustomRiveAnimation(
                    modifier = modifier,
                    url = spec.url,
                    alignment = alignment,
                    autoPlay = autoPlay,
                    artboardName = artboardName,
                    fit = fit,
                    stateMachineName = stateMachineName
                )
            }

            is RiveByteArrayCompositionSpec -> {
                CustomRiveAnimation(
                    modifier = modifier,
                    byteArray = spec.byteArray,
                    alignment = alignment,
                    autoPlay = autoPlay,
                    artboardName = artboardName,
                    fit = fit,
                    stateMachineName = stateMachineName
                )
            }
        }
    }
}

@ExperimentalRiveCmpApi
@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    url: String,
    alignment: RiveAlignment = RiveAlignment.CENTER,
    autoPlay: Boolean = true,
    artboardName: String? = null,
    fit: RiveFit = RiveFit.CONTAIN,
    stateMachineName: String? = null,
)

@ExperimentalRiveCmpApi
@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment = RiveAlignment.CENTER,
    autoPlay: Boolean = true,
    artboardName: String? = null,
    fit: RiveFit = RiveFit.CONTAIN,
    stateMachineName: String? = null,
)