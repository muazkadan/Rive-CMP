@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.muazkadan.rivecmp

fun emptyRiveLayoutOptions(): RiveLayoutOptions = js("({})")

fun emptyRiveOptions(): RiveOptions = js("({})")

fun emptyResetOptions(): JsAny = js("({})")

fun createRiveLayout(options: RiveLayoutOptions): RiveLayout = RiveLayout(options)

fun createRive(options: RiveOptions): Rive = Rive(options)
