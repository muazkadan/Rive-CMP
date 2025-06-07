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
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import nativeIosShared.RiveAnimationController
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {
    if (composition != null) {
        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                val animationController = remember(spec.url, autoPlay, artboardName, fit, stateMachineName, alignment) {
                    val controller = RiveAnimationController()
                    controller.setAnimationItemWithUrl(
                        url = spec.url,
                        autoPlay = autoPlay,
                        artboardName = artboardName,
                        stateMachineName = stateMachineName,
                        fit = fit.toIosFit(),
                        alignment = alignment.toIosAlignment()
                    )
                    composition.connectToAnimationView(controller)
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
            is RiveByteArrayCompositionSpec -> {
                val animationController = remember(spec.byteArray, autoPlay, artboardName, fit, stateMachineName, alignment) {
                    val controller = RiveAnimationController()

                    // Convert ByteArray to NSData
                    val nsData = spec.byteArray.usePinned { pinned ->
                        NSData.create(
                            bytes = pinned.addressOf(0),
                            length = spec.byteArray.size.toULong()
                        )
                    }

                    controller.setAnimationItemWithData(
                        data = nsData,
                        autoPlay = autoPlay,
                        artboardName = artboardName,
                        stateMachineName = stateMachineName,
                        fit = fit.toIosFit(),
                        alignment = alignment.toIosAlignment()
                    )
                    composition.connectToAnimationView(controller)
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
        }
    }
}

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
        controller.setAnimationItemWithUrl(
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


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {
    val animationController = remember(byteArray, autoPlay, artboardName, fit, stateMachineName, alignment) {
        val controller = RiveAnimationController()

        // Convert ByteArray to NSData
        val nsData = byteArray.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = byteArray.size.toULong()
            )
        }

        controller.setAnimationItemWithData(
            data = nsData,
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