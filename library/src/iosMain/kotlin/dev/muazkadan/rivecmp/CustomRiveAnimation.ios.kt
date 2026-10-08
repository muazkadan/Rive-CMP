package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
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
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference
import nativeIosShared.RiveAnimationController
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalComposeUiApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)?,
) {
    if (composition != null) {
        val currentOnViewModelInstance by rememberUpdatedState(onViewModelInstance)
        val autoBind = onViewModelInstance != null
        val boundInstances = remember { mutableMapOf<RiveAnimationController, MutableList<IosRiveViewModelInstance>>() }

        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                val animationController = remember(spec.url, autoPlay, artboardName, fit, stateMachineName, alignment, autoBind) {
                    val controller = RiveAnimationController()
                    if (autoBind) {
                        bindViewModelInstance(controller, boundInstances.getOrPut(controller) { mutableListOf() }) { currentOnViewModelInstance?.invoke(it) }
                    }
                    controller.setAnimationItemWithUrl(
                        url = spec.url,
                        autoPlay = autoPlay,
                        artboardName = artboardName,
                        stateMachineName = stateMachineName,
                        fit = fit.toIosFit(),
                        alignment = alignment.toIosAlignment()
                    )
                    controller
                }

                // Keyed on the controller so a replaced controller is released, and connected here so
                // the replaced one's disposal cannot disconnect its successor.
                DisposableEffect(animationController) {
                    composition.connectToAnimationView(animationController)
                    onDispose {
                        boundInstances.remove(animationController)?.releaseAll()
                        // Disconnect composition first to prevent calls on released controller
                        composition.connectToAnimationView(null)
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
                    },
                    properties = UIKitInteropProperties(placedAsOverlay = overlay)
                )
            }
            is RiveByteArrayCompositionSpec -> {
                val animationController = remember(spec.byteArray, autoPlay, artboardName, fit, stateMachineName, alignment, autoBind) {
                    val controller = RiveAnimationController()
                    if (autoBind) {
                        bindViewModelInstance(controller, boundInstances.getOrPut(controller) { mutableListOf() }) { currentOnViewModelInstance?.invoke(it) }
                    }

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
                    controller
                }

                // Keyed on the controller so a replaced controller is released, and connected here so
                // the replaced one's disposal cannot disconnect its successor.
                DisposableEffect(animationController) {
                    composition.connectToAnimationView(animationController)
                    onDispose {
                        boundInstances.remove(animationController)?.releaseAll()
                        // Disconnect composition first to prevent calls on released controller
                        composition.connectToAnimationView(null)
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
                    },
                    properties = UIKitInteropProperties(placedAsOverlay = overlay)
                )
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)?,
) {
    val currentOnViewModelInstance by rememberUpdatedState(onViewModelInstance)
    val autoBind = onViewModelInstance != null
    val boundInstances = remember { mutableMapOf<RiveAnimationController, MutableList<IosRiveViewModelInstance>>() }

    val animationController = remember(url, autoPlay, artboardName, fit, stateMachineName, alignment, autoBind) {
        val controller = RiveAnimationController()
        if (autoBind) {
            bindViewModelInstance(controller, boundInstances.getOrPut(controller) { mutableListOf() }) { currentOnViewModelInstance?.invoke(it) }
        }
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

    // Keyed on the controller so a replaced controller is released.
    DisposableEffect(animationController) {
        onDispose {
            boundInstances.remove(animationController)?.releaseAll()
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
        },
        properties = UIKitInteropProperties(placedAsOverlay = overlay)
    )
}


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalComposeUiApi::class)
@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)?,
) {
    val currentOnViewModelInstance by rememberUpdatedState(onViewModelInstance)
    val autoBind = onViewModelInstance != null
    val boundInstances = remember { mutableMapOf<RiveAnimationController, MutableList<IosRiveViewModelInstance>>() }

    val animationController = remember(byteArray, autoPlay, artboardName, fit, stateMachineName, alignment, autoBind) {
        val controller = RiveAnimationController()
        if (autoBind) {
            bindViewModelInstance(controller, boundInstances.getOrPut(controller) { mutableListOf() }) { currentOnViewModelInstance?.invoke(it) }
        }

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

    // Keyed on the controller so a replaced controller is released.
    DisposableEffect(animationController) {
        onDispose {
            boundInstances.remove(animationController)?.releaseAll()
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
        },
        properties = UIKitInteropProperties(placedAsOverlay = overlay)
    )
}

/**
 * Asks [controller] to auto-bind and hand each bound instance to [onViewModelInstance], keeping it
 * in [bound] so its listeners can be released. Must run before the controller is given its
 * animation. The controller calls back on a later turn of the main run loop, never during
 * composition.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalRiveCmpApi::class, ExperimentalNativeApi::class)
private fun bindViewModelInstance(
    controller: RiveAnimationController,
    bound: MutableList<IosRiveViewModelInstance>,
    onViewModelInstance: (RiveViewModelInstance) -> Unit,
) {
    // The controller keeps this callback, so the callback must not keep the controller: a cycle
    // through an Objective-C block is not reliably collected and would leak both.
    val controllerRef = WeakReference(controller)
    controller.setOnViewModelInstance { instance ->
        if (instance != null) {
            // A new instance replaces the one bound before, for example after a reset.
            bound.releaseAll()
            IosRiveViewModelInstance(instance, onWrite = { controllerRef.get()?.resumePlayback() })
                .also { bound += it }
                .let(onViewModelInstance)
        }
    }
}

private fun MutableList<IosRiveViewModelInstance>.releaseAll() {
    forEach { it.release() }
    clear()
}
