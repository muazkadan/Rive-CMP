package dev.muazkadan.rivecmp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.IntSize
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toJvmAlignment
import dev.muazkadan.rivecmp.core.toJvmFit
import dev.muazkadan.rivecmp.native.RiveFileController
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap
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
) {
    if (composition == null) return

    val controller =
        remember(composition, alignment, autoPlay, artboardName, fit, stateMachineName) {
            RiveFileController(
                autoplay = autoPlay,
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

    Spacer(modifier.then(RiveRendererElement(controller)))
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
) {
    val composition by rememberRiveComposition(url) { RiveCompositionSpec.url(url) }

    // This overload owns the composition it creates (the caller never sees it), so - unlike the
    // `composition: RiveComposition?` overload above, where the caller manages the composition's
    // lifecycle - it's responsible for releasing its native File when done with it.
    DisposableEffect(composition) {
        onDispose { composition?.file?.release() }
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
) {
    val composition by rememberRiveComposition(byteArray) { RiveCompositionSpec.byteArray(byteArray) }

    DisposableEffect(composition) {
        onDispose { composition?.file?.release() }
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
    )
}

private data class RiveRendererElement(
    private val controller: RiveFileController,
) : ModifierNodeElement<RiveRendererNode>() {

    override fun create(): RiveRendererNode = RiveRendererNode(controller)

    override fun update(node: RiveRendererNode) {
        node.controller = controller
    }
}

/**
 * Draws a [RiveFileController]'s artboard, advancing it on every UI frame.
 *
 * The native renderer writes directly into the [Bitmap]'s pixel memory, so a
 * frame costs no pixel copies: advance, [Bitmap.notifyPixelsChanged], [invalidateDraw].
 */
private class RiveRendererNode(
    controller: RiveFileController,
) : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode {

    var controller: RiveFileController = controller
        set(value) {
            if (field === value) return
            field = value
            bindBuffer()
        }

    private var bitmap: Bitmap? = null
    private var imageBitmap: ImageBitmap? = null

    override fun onRemeasured(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        if (bitmap?.width == size.width && bitmap?.height == size.height) return

        bitmap?.close()
        bitmap = Bitmap()
            .apply { allocPixels(ImageInfo.makeN32Premul(size.width, size.height)) }
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
        imageBitmap?.let(::drawImage)
        drawContent()
    }
}
