package dev.muazkadan.rivecmp.core

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Maps common RiveFit to iOS-specific RiveFit
 */
@OptIn(ExperimentalForeignApi::class)
internal fun RiveFit.toIosFit(): RiveRuntime.RiveFit = when (this) {
    RiveFit.FILL -> RiveRuntime.RiveFit.fill
    RiveFit.CONTAIN -> RiveRuntime.RiveFit.contain
    RiveFit.COVER -> RiveRuntime.RiveFit.cover
    RiveFit.FIT_WIDTH -> RiveRuntime.RiveFit.fitWidth
    RiveFit.FIT_HEIGHT -> RiveRuntime.RiveFit.fitHeight
    RiveFit.NONE -> RiveRuntime.RiveFit.noFit
}
