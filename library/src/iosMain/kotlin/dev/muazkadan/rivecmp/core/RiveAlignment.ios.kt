package dev.muazkadan.rivecmp.core

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Maps common RiveAlignment to iOS-specific RiveAlignment
 */
@OptIn(ExperimentalForeignApi::class)
internal fun RiveAlignment.toIosAlignment(): RiveRuntime.RiveAlignment =
    when (this) {
        RiveAlignment.TOP_LEFT -> RiveRuntime.RiveAlignment.topLeft
        RiveAlignment.TOP_CENTER -> RiveRuntime.RiveAlignment.topCenter
        RiveAlignment.TOP_RIGHT -> RiveRuntime.RiveAlignment.topRight
        RiveAlignment.CENTER_LEFT -> RiveRuntime.RiveAlignment.centerLeft
        RiveAlignment.CENTER -> RiveRuntime.RiveAlignment.center
        RiveAlignment.CENTER_RIGHT -> RiveRuntime.RiveAlignment.centerRight
        RiveAlignment.BOTTOM_LEFT -> RiveRuntime.RiveAlignment.bottomLeft
        RiveAlignment.BOTTOM_CENTER -> RiveRuntime.RiveAlignment.bottomCenter
        RiveAlignment.BOTTOM_RIGHT -> RiveRuntime.RiveAlignment.bottomRight
    }
