package dev.muazkadan.rivecmp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toJvmAlignment
import dev.muazkadan.rivecmp.core.toJvmFit
import dev.muazkadan.rivecmp.native.RiveFileController
import dev.muazkadan.rivecmp.native.ViewModelInstance
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.BufferUtil

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
    if (composition == null) return

    val currentOnViewModelInstance by rememberUpdatedState(onViewModelInstance)
    val autoBind = onViewModelInstance != null

    val controller =
        remember(composition, alignment, autoPlay, artboardName, fit, stateMachineName, autoBind) {
            RiveFileController(
                autoplay = autoPlay,
                autoBind = autoBind,
                stateMachineName = stateMachineName,
                file = composition.file,
                artboard = artboardName?.let(composition.file::artboard)
                    ?: composition.file.firstArtboard,
                alignment = alignment.toJvmAlignment(),
                fit = fit.toJvmFit(),
            )
        }

    DisposableEffect(controller) {
        // Connect composition to this instance.
        composition.connectToAnimationView(controller)
        onDispose {
            // Disconnect composition first to prevent calls on cleaned-up instance.
            composition.connectToAnimationView(null)
            controller.dispose()
        }
    }

    DisposableEffect(controller) {
        val deliver = { instance: ViewModelInstance ->
            currentOnViewModelInstance?.invoke(
                DesktopRiveViewModelInstance(instance, controller::resumeStateMachines),
            )
            Unit
        }
        controller.viewModelInstance?.let(deliver)
        // Resetting the composition instances the artboard again and binds a fresh instance.
        controller.onViewModelInstanceBound = deliver
        onDispose { controller.onViewModelInstanceBound = null }
    }

    Spacer(modifier.then(RiveRendererElement(controller, overlay)))
}

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
    val composition by rememberRiveComposition(url) { RiveCompositionSpec.url(url) }

    // This overload owns the composition it creates (the caller never sees it), so - unlike the
    // `composition: RiveComposition?` overload above, where the caller manages the composition's
    // lifecycle - it's responsible for releasing its native File when done with it.
    DisposableEffect(composition) {
        // Captured so this effect releases the File it was keyed on, not the one loaded after it
        val file = composition?.file
        onDispose { file?.release() }
    }

    CustomRiveAnimation(
        modifier = modifier,
        composition = composition,
        alignment = alignment,
        autoPlay = autoPlay,
        artboardName = artboardName,
        fit = fit,
        stateMachineName = stateMachineName,
        overlay = overlay,
        onViewModelInstance = onViewModelInstance,
    )
}

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
    val composition by rememberRiveComposition(byteArray) { RiveCompositionSpec.byteArray(byteArray) }

    DisposableEffect(composition) {
        // Captured so this effect releases the File it was keyed on, not the one loaded after it
        val file = composition?.file
        onDispose { file?.release() }
    }

    CustomRiveAnimation(
        modifier = modifier,
        composition = composition,
        alignment = alignment,
        autoPlay = autoPlay,
        artboardName = artboardName,
        fit = fit,
        stateMachineName = stateMachineName,
        overlay = overlay,
        onViewModelInstance = onViewModelInstance,
    )
}

private data class RiveRendererElement(
    private val controller: RiveFileController,
    private val overlay: Boolean,
) : ModifierNodeElement<RiveRendererNode>() {

    override fun create(): RiveRendererNode = RiveRendererNode(controller, overlay)

    override fun update(node: RiveRendererNode) {
        node.controller = controller
        node.overlay = overlay
    }
}

/**
 * Draws a [RiveFileController]'s artboard, advancing it on every UI frame, and sizes the view to
 * the artboard as [measureArtboard] describes.
 *
 * The native renderer writes directly into the [Bitmap]'s pixel memory, so a
 * frame costs no pixel copies: advance, [Bitmap.notifyPixelsChanged], [invalidateDraw].
 */
private class RiveRendererNode(
    controller: RiveFileController,
    overlay: Boolean,
) : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode, LayoutModifierNode {

    var controller: RiveFileController = controller
        set(value) {
            if (field === value) return
            field = value
            invalidateMeasurement()
            bindBuffer()
        }

    var overlay: Boolean = overlay

    private val TRANSPARENT = 0

    private var bitmap: Bitmap? = null
    private var imageBitmap: ImageBitmap? = null

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val artboard = controller.artboard
        val size = measureArtboard(
            constraints = constraints,
            artboard = ArtboardSize(artboard.width, artboard.height),
            fit = controller.fit,
            scaleFactor = controller.layoutScaleFactorActive,
        )
        val placeable = measurable.measure(Constraints.fixed(size.width, size.height))
        return layout(size.width, size.height) { placeable.place(0, 0) }
    }

    override fun onRemeasured(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        if (bitmap?.width == size.width && bitmap?.height == size.height) return

        bitmap?.close()
        bitmap = Bitmap()
            .apply {
                // The native bridge links its own Skia build, whose kN32_SkColorType
                // need not match Skiko's N32. Pin both sides to an explicit order.
                allocPixels(
                    ImageInfo(size.width, size.height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
                )
            }
            .also { imageBitmap = it.asComposeImageBitmap() }
        bindBuffer()
    }

    /** Points the native renderer at the bitmap's pixel memory. */
    private fun bindBuffer() {
        val bitmap = bitmap ?: return
        val pixels = requireNotNull(bitmap.peekPixels()) { "Rive bitmap has no pixel storage" }
        controller.artboardRenderer.setBuffer(
            BufferUtil.getByteBufferFromPointer(pixels.addr, bitmap.rowBytes * bitmap.height),
            bitmap.width,
            bitmap.height,
        )
    }

    override fun onAttach() {
        coroutineScope.launch {
            var lastFrameTime = withFrameMillis { it }
            while (isActive) {
                withFrameMillis { frameTime ->
                    if (controller.artboardRenderer.isPlaying) {
                        // Rive draws only the artboard's shapes into the bitmap and never
                        // clears it, so anything transparent in this frame would otherwise
                        // keep showing the previous frame's pixels.
                        bitmap?.erase(TRANSPARENT)
                        controller.artboardRenderer.doFrame((frameTime - lastFrameTime) / 1_000f)
                        bitmap?.notifyPixelsChanged()
                        invalidateDraw()
                    }
                    lastFrameTime = frameTime
                }
            }
        }
    }

    override fun onDetach() {
        bitmap?.close()
        bitmap = null
        imageBitmap = null
    }

    override fun ContentDrawScope.draw() {
        if (overlay) {
            drawContent()
            imageBitmap?.let(::drawImage)
        } else {
            imageBitmap?.let(::drawImage)
            drawContent()
        }
    }
}
