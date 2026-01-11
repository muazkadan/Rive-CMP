package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.browser.document
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.unsafeCast

@OptIn(ExperimentalRiveCmpApi::class, ExperimentalComposeUiApi::class)
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
    if (composition == null) return

    val canvas = remember { document.createElement("canvas") as HTMLCanvasElement }


    WebElementView(
        factory = {
            canvas
        },
        modifier = modifier,
        update = {
            // Update logic if needed
        }
    )

    val riveFit = when (fit) {
        RiveFit.FILL -> RiveSDK.Fit.Fill
        RiveFit.CONTAIN -> RiveSDK.Fit.Contain
        RiveFit.COVER -> RiveSDK.Fit.Cover
        RiveFit.FIT_WIDTH -> RiveSDK.Fit.FitWidth
        RiveFit.FIT_HEIGHT -> RiveSDK.Fit.FitHeight
        RiveFit.NONE -> RiveSDK.Fit.None
        // Fallback or map SCALE_DOWN
        else -> RiveSDK.Fit.Contain
    }

    val riveAlignment = when (alignment) {
        RiveAlignment.TOP_LEFT -> RiveSDK.Alignment.TopLeft
        RiveAlignment.TOP_CENTER -> RiveSDK.Alignment.TopCenter
        RiveAlignment.TOP_RIGHT -> RiveSDK.Alignment.TopRight
        RiveAlignment.CENTER_LEFT -> RiveSDK.Alignment.CenterLeft
        RiveAlignment.CENTER -> RiveSDK.Alignment.Center
        RiveAlignment.CENTER_RIGHT -> RiveSDK.Alignment.CenterRight
        RiveAlignment.BOTTOM_LEFT -> RiveSDK.Alignment.BottomLeft
        RiveAlignment.BOTTOM_CENTER -> RiveSDK.Alignment.BottomCenter
        RiveAlignment.BOTTOM_RIGHT -> RiveSDK.Alignment.BottomRight
    }

    DisposableEffect(composition.spec, canvas, fit, alignment, autoPlay, artboardName, stateMachineName) {
        val layoutOptions = js("{}")
        layoutOptions.fit = riveFit
        layoutOptions.alignment = riveAlignment
        val layout = RiveSDK.Layout(layoutOptions)
        
        val options = js("{}")
        options.canvas = canvas
        options.autoplay = autoPlay
        options.layout = layout
        
        if (stateMachineName != null) {
            options.stateMachines = stateMachineName
        }
        if (artboardName != null) {
            options.artboard = artboardName
        }

        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                options.src = spec.url
            }
            is RiveByteArrayCompositionSpec -> {
                // Convert ByteArray to Uint8Array/ArrayBuffer for JS
                val buffer = spec.byteArray.toJsUint8Array()
                options.buffer = buffer
            }
        }

        var r: RiveSDK.Rive? = null

        // Add onLoad callback to ensure the drawing surface matches the canvas size
        options.onLoad = {
            r?.resizeDrawingSurfaceToCanvas()
        }

        r = RiveSDK.Rive(options)

        // Connect composition to this instance
        composition.connectToAnimationView(r)

        onDispose {
            // Disconnect composition first to prevent calls on cleaned-up instance
            composition.connectToAnimationView(null)
            r.stop()
            r.cleanup()
        }
    }
}

// Helper to convert ByteArray to Uint8Array
private fun ByteArray.toJsUint8Array(): Uint8Array {
    // We cast to Int8Array first because that's what ByteArray is under the hood
    val i8 = this.unsafeCast<Int8Array>()
    // Create a new Uint8Array using the existing buffer
    return Uint8Array(i8.buffer, i8.byteOffset, i8.length)
}

@OptIn(ExperimentalRiveCmpApi::class)
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
    val composition = rememberRiveComposition(url) {
        RiveCompositionSpec.url(url)
    }
    CustomRiveAnimation(
        modifier = modifier,
        composition = composition.value, // usage depends on API, rememberRiveComposition returns RiveCompositionResult
        alignment = alignment,
        autoPlay = autoPlay,
        artboardName = artboardName,
        fit = fit,
        stateMachineName = stateMachineName
    )
}

@OptIn(ExperimentalRiveCmpApi::class)
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
    val composition = rememberRiveComposition(byteArray) {
        RiveCompositionSpec.byteArray(byteArray)
    }
    CustomRiveAnimation(
        modifier = modifier,
        composition = composition.value,
        alignment = alignment,
        autoPlay = autoPlay,
        artboardName = artboardName,
        fit = fit,
        stateMachineName = stateMachineName
    )
}