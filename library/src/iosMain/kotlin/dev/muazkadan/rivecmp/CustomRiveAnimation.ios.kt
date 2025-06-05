package dev.muazkadan.rivecmp

import RiveRuntime.RiveAlignment
import RiveRuntime.RiveViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toIosFit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {
    val viewModel = remember(url, autoPlay, artboardName, fit, stateMachineName) {
        RiveViewModel(
            webURL = url,
            stateMachineName = stateMachineName,
            fit = fit.toIosFit(),
            autoPlay = autoPlay,
            alignment = RiveAlignment.center,
            loadCdn = false,
            artboardName = artboardName
        )
    }
    UIKitView(
        factory = {
            viewModel.createRiveView()
        },
        modifier = modifier,
        update = { view ->
            viewModel.updateWithView(view)
        }
    )
}