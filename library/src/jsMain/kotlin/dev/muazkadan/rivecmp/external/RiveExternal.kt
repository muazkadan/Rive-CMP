@file:JsModule("@rive-app/canvas")
@file:JsNonModule

package dev.muazkadan.rivecmp.external

import web.html.HTMLCanvasElement

@JsName("Rive")
external class RiveClass(options: RiveOptions) {
    fun play()
    fun pause()
    fun cleanup()
}

external interface RiveOptions {
    var src: String
    var canvas: HTMLCanvasElement
    var autoplay: Boolean?
    var loop: Boolean?
    var onLoad: (() -> Unit)?
    var onLoadError: ((error: String) -> Unit)?
}