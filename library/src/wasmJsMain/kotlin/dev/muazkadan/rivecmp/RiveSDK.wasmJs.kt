@file:JsModule("@rive-app/canvas")
@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.muazkadan.rivecmp

import org.w3c.dom.HTMLCanvasElement

@JsName("Rive")
external class Rive(options: RiveOptions) : JsAny {
    fun play()
    fun pause()
    fun stop()
    fun stateMachineInputs(name: String): JsAny?
    fun cleanup()
    fun resizeToCanvas()
    fun resizeDrawingSurfaceToCanvas()
    fun reset(params: JsAny)
}

@JsName("Layout")
external class RiveLayout(options: RiveLayoutOptions = definedExternally) : JsAny

external interface RiveLayoutOptions : JsAny {
    var fit: String?
    var alignment: String?
}

external interface RiveOptions : JsAny {
    var canvas: HTMLCanvasElement?
    var autoplay: Boolean?
    var layout: RiveLayout?
    var stateMachines: String?
    var artboard: String?
    var src: String?
    var buffer: JsAny?
    var onLoad: (() -> Unit)?
}
