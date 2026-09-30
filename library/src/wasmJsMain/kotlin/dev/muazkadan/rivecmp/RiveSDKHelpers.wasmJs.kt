@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.muazkadan.rivecmp

fun emptyRiveLayoutOptions(): RiveLayoutOptions = js("({})")

fun emptyRiveOptions(): RiveOptions = js("({})")

fun resetOptions(autoBind: Boolean): JsAny = js("({ autoBind: autoBind })")

fun createRiveLayout(options: RiveLayoutOptions): RiveLayout = RiveLayout(options)

fun createRive(options: RiveOptions): Rive = Rive(options)
