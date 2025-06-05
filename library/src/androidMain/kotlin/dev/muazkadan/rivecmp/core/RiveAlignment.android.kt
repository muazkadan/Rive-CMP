package dev.muazkadan.rivecmp.core

import app.rive.runtime.kotlin.core.Alignment

/**
 * Maps common RiveAlignment to Android-specific Alignment
 */
internal fun RiveAlignment.toAndroidAlignment(): Alignment = when (this) {
    RiveAlignment.TOP_LEFT -> Alignment.TOP_LEFT
    RiveAlignment.TOP_CENTER -> Alignment.TOP_CENTER
    RiveAlignment.TOP_RIGHT -> Alignment.TOP_RIGHT
    RiveAlignment.CENTER_LEFT -> Alignment.CENTER_LEFT
    RiveAlignment.CENTER -> Alignment.CENTER
    RiveAlignment.CENTER_RIGHT -> Alignment.CENTER_RIGHT
    RiveAlignment.BOTTOM_LEFT -> Alignment.BOTTOM_LEFT
    RiveAlignment.BOTTOM_CENTER -> Alignment.BOTTOM_CENTER
    RiveAlignment.BOTTOM_RIGHT -> Alignment.BOTTOM_RIGHT
}
