@file:OptIn(ExperimentalWasmJsInterop::class)

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
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.wasmMemory
import kotlin.wasm.unsafe.withScopedMemoryAllocator
import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement

@OptIn(ExperimentalRiveCmpApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean
) {
    if (composition == null) return

    val canvas = remember { document.createElement("canvas") as HTMLCanvasElement }

    WebElementView(
        factory = {
            canvas
        },
        modifier = modifier,
        update = {
        }
    )

    val riveFit = when (fit) {
        RiveFit.FILL -> "fill"
        RiveFit.CONTAIN -> "contain"
        RiveFit.COVER -> "cover"
        RiveFit.FIT_WIDTH -> "fitWidth"
        RiveFit.FIT_HEIGHT -> "fitHeight"
        RiveFit.NONE -> "none"
    }

    val riveAlignment = when (alignment) {
        RiveAlignment.TOP_LEFT -> "topLeft"
        RiveAlignment.TOP_CENTER -> "topCenter"
        RiveAlignment.TOP_RIGHT -> "topRight"
        RiveAlignment.CENTER_LEFT -> "centerLeft"
        RiveAlignment.CENTER -> "center"
        RiveAlignment.CENTER_RIGHT -> "centerRight"
        RiveAlignment.BOTTOM_LEFT -> "bottomLeft"
        RiveAlignment.BOTTOM_CENTER -> "bottomCenter"
        RiveAlignment.BOTTOM_RIGHT -> "bottomRight"
    }

    DisposableEffect(composition.spec, canvas, fit, alignment, autoPlay, artboardName, stateMachineName) {
        val layoutOptions = emptyRiveLayoutOptions().apply {
            this.fit = riveFit
            this.alignment = riveAlignment
        }
        val layout = createRiveLayout(layoutOptions)

        val options = emptyRiveOptions().apply {
            this.canvas = canvas
            this.autoplay = autoPlay
            this.layout = layout

            if (stateMachineName != null) {
                this.stateMachines = stateMachineName
            }
            if (artboardName != null) {
                this.artboard = artboardName
            }
        }

        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                options.src = spec.url
            }
            is RiveByteArrayCompositionSpec -> {
                options.buffer = byteArrayToJsUint8Array(spec.byteArray)
            }
        }

        var r: Rive? = null

        options.onLoad = {
            r?.resizeDrawingSurfaceToCanvas()
        }

        r = createRive(options)
        composition.connectToAnimationView(r)

        onDispose {
            composition.connectToAnimationView(null)
            r.stop()
            r.cleanup()
        }
    }
}

@OptIn(UnsafeWasmMemoryApi::class)
private fun byteArrayToJsUint8Array(byteArray: ByteArray): JsAny =
    withScopedMemoryAllocator { allocator ->
        val size = byteArray.size
        val pointer = allocator.allocate(size)
        for (i in 0 until size) {
            (pointer + i).storeByte(byteArray[i])
        }
        wasmMemoryToJsUint8Array(wasmMemory, pointer.address.toInt(), size)
    }

private fun wasmMemoryToJsUint8Array(memory: JsAny, pointer: Int, size: Int): JsAny =
    js("new Uint8Array(memory.buffer, pointer, size).slice()")

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean
) {
    val composition = rememberRiveComposition(url) {
        RiveCompositionSpec.url(url)
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

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean
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
