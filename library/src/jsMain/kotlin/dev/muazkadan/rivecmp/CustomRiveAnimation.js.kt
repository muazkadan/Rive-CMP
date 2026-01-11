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

// Import declaration for the external Rive library
// Since we are using npm, we access it via dynamic for simplicity or define external class
// We assume 'rive' is available globally or required.
// For KMP with NPM, we usually access via js("require('@rive-app/canvas')") or similar if using CommonJS/UMD
// but in ES modules (Kotlin 2.0+ default), it might be different.
// The safest way in a library without forcing a specific loader is to use the implementation dependency
// and access the module via @JsModule if possible, or dynamic import.
// Using a dynamic require approach for now to avoid complex externals setup.

@JsModule("@rive-app/canvas")
@JsNonModule
external object RiveSDK {
    class Rive(options: dynamic) {
        fun play()
        fun pause()
        fun stop()
        fun stateMachineInputs(name: String): Array<dynamic>
        fun cleanup()
        fun resizeToCanvas()
    }
    object Fit {
        val COVER: dynamic
        val CONTAIN: dynamic
        val FILL: dynamic
        val FIT_WIDTH: dynamic
        val FIT_HEIGHT: dynamic
        val NONE: dynamic
        val SCALE_DOWN: dynamic
    }
    object Alignment {
        val CENTER: dynamic
        val TOP_LEFT: dynamic
        val TOP_CENTER: dynamic
        val TOP_RIGHT: dynamic
        val CENTER_LEFT: dynamic
        val CENTER_RIGHT: dynamic
        val BOTTOM_LEFT: dynamic
        val BOTTOM_CENTER: dynamic
        val BOTTOM_RIGHT: dynamic
    }
    class Layout(fit: dynamic, alignment: dynamic)
}

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
        RiveFit.FILL -> RiveSDK.Fit.FILL
        RiveFit.CONTAIN -> RiveSDK.Fit.CONTAIN
        RiveFit.COVER -> RiveSDK.Fit.COVER
        RiveFit.FIT_WIDTH -> RiveSDK.Fit.FIT_WIDTH
        RiveFit.FIT_HEIGHT -> RiveSDK.Fit.FIT_HEIGHT
        RiveFit.NONE -> RiveSDK.Fit.NONE
        // Fallback or map SCALE_DOWN
        else -> RiveSDK.Fit.CONTAIN
    }

    val riveAlignment = when (alignment) {
        RiveAlignment.TOP_LEFT -> RiveSDK.Alignment.TOP_LEFT
        RiveAlignment.TOP_CENTER -> RiveSDK.Alignment.TOP_CENTER
        RiveAlignment.TOP_RIGHT -> RiveSDK.Alignment.TOP_RIGHT
        RiveAlignment.CENTER_LEFT -> RiveSDK.Alignment.CENTER_LEFT
        RiveAlignment.CENTER -> RiveSDK.Alignment.CENTER
        RiveAlignment.CENTER_RIGHT -> RiveSDK.Alignment.CENTER_RIGHT
        RiveAlignment.BOTTOM_LEFT -> RiveSDK.Alignment.BOTTOM_LEFT
        RiveAlignment.BOTTOM_CENTER -> RiveSDK.Alignment.BOTTOM_CENTER
        RiveAlignment.BOTTOM_RIGHT -> RiveSDK.Alignment.BOTTOM_RIGHT
    }

    DisposableEffect(composition.spec, canvas) {
        val layout = RiveSDK.Layout(riveFit, riveAlignment)
        
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

        val r = RiveSDK.Rive(options)
        
        // Connect composition to this instance
        composition.connectToAnimationView(r)

        onDispose {
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