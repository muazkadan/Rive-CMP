package dev.muazkadan.rivecmp.core

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Maps common RiveAlignment to iOS-specific RiveAlignment
 */
@OptIn(ExperimentalForeignApi::class)
internal fun RiveAlignment.toIosAlignment(): nativeIosShared.RiveAlignment =
    when (this) {
        RiveAlignment.TOP_LEFT -> nativeIosShared.RiveAlignment.topLeft
        RiveAlignment.TOP_CENTER -> nativeIosShared.RiveAlignment.topCenter
        RiveAlignment.TOP_RIGHT -> nativeIosShared.RiveAlignment.topRight
        RiveAlignment.CENTER_LEFT -> nativeIosShared.RiveAlignment.centerLeft
        RiveAlignment.CENTER -> nativeIosShared.RiveAlignment.center
        RiveAlignment.CENTER_RIGHT -> nativeIosShared.RiveAlignment.centerRight
        RiveAlignment.BOTTOM_LEFT -> nativeIosShared.RiveAlignment.bottomLeft
        RiveAlignment.BOTTOM_CENTER -> nativeIosShared.RiveAlignment.bottomCenter
        RiveAlignment.BOTTOM_RIGHT -> nativeIosShared.RiveAlignment.bottomRight
    }
