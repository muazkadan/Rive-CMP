package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toIosAlignment
import dev.muazkadan.rivecmp.core.toIosFit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.cinterop.ExperimentalForeignApi
import nativeIosShared.RiveAnimationController

@OptIn(ExperimentalForeignApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {
    val animationController = remember(url, autoPlay, artboardName, fit, stateMachineName, alignment) {
        val controller = RiveAnimationController()
        controller.setAnimationItemWithUrlWithUrl(
            url = url,
            autoPlay = autoPlay,
            artboardName = artboardName,
            stateMachineName = stateMachineName,
            fit = fit.toIosFit(),
            alignment = alignment.toIosAlignment()
        )
        controller
    }

    DisposableEffect(Unit) {
        onDispose {
            animationController.releaseAnimation()
        }
    }

    UIKitView(
        factory = {
            animationController.createAnimationView()
        },
        modifier = modifier,
        update = { view ->
            animationController.updateView(view)
        }
    )
}