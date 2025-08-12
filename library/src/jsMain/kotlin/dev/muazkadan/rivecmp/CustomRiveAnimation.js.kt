package dev.muazkadan.rivecmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.external.RiveClass
import dev.muazkadan.rivecmp.external.RiveOptions
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import dev.muazkadan.rivecmp.webInterlop.HtmlView
import web.console.console
import web.dom.ElementId
import web.dom.document
import web.html.HTMLCanvasElement
import kotlin.js.js
import kotlin.random.Random

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
    if (composition == null) return

    val url = when (val spec = composition.spec) {
        is RiveUrlCompositionSpec -> spec.url
        is RiveByteArrayCompositionSpec -> {
            console.warn("Rive byte array source is not supported on web yet; skipping animation")
            null
        }
        else -> null
    }

    if (url == null) {
        // Render empty placeholder to maintain layout
        HtmlView(modifier = modifier.fillMaxSize(), factory = { document.createElement("div") })
        return
    }

    val canvasElement = remember {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.id = ElementId("rive-canvas-${Random.nextInt()}")
        canvas.style.width = "100%"
        canvas.style.height = "100%"
        canvas
    }

    val riveInstance = remember(url) {
        val options = js("({})") as RiveOptions
        options.src = url
        options.canvas = canvasElement
        options.autoplay = autoPlay
        options.onLoad = {
            console.log("Rive animation loaded successfully")
        }
        options.onLoadError = { error ->
            console.error("Failed to load Rive animation: $error")
        }
        RiveClass(options)
    }

    // Wire composition controls to this instance
    DisposableEffect(riveInstance) {
        composition.connectToAnimationView(riveInstance)
        onDispose {
            composition.connectToAnimationView(null)
            riveInstance.cleanup()
        }
    }

    HtmlView(
        modifier = modifier.fillMaxSize(),
        factory = { canvasElement }
    )
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
    stateMachineName: String?
) {
    val canvasElement = remember {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.id = ElementId("rive-canvas-${Random.nextInt()}")
        canvas.style.width = "100%"
        canvas.style.height = "100%"
        canvas
    }

    val riveInstance = remember(url, autoPlay) {
        val options = js("({})") as RiveOptions
        options.src = url
        options.canvas = canvasElement
        options.autoplay = autoPlay
        options.onLoad = {
            console.log("Rive animation loaded successfully")
        }
        options.onLoadError = { error ->
            console.error("Failed to load Rive animation: $error")
        }
        RiveClass(options)
    }

    DisposableEffect(riveInstance) {
        onDispose {
            riveInstance.cleanup()
        }
    }

    HtmlView(
        modifier = modifier.fillMaxSize(),
        factory = { canvasElement }
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
    stateMachineName: String?
) {
    // Not supported in current minimal web interop: log and render empty placeholder
    console.warn("CustomRiveAnimation(byteArray) is not supported on web yet")
    HtmlView(modifier = modifier.fillMaxSize(), factory = { document.createElement("div") })
}