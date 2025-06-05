package dev.muazkadan.rivecmp

import RiveRuntime.RiveAlignment
import RiveRuntime.RiveFit
import RiveRuntime.RiveViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    stateMachineName: String?
) {
    val viewModel = remember {
        RiveViewModel(
            webURL = url,
            stateMachineName = stateMachineName,
            fit = RiveFit.contain,
            autoPlay = true,
            alignment = RiveAlignment.center,
            loadCdn = false,
            artboardName = null
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