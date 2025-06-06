package dev.muazkadan.rivecmp.core

import kotlinx.cinterop.ExperimentalForeignApi
/**
 * Maps common RiveFit to iOS-specific RiveFit
 */
@OptIn(ExperimentalForeignApi::class)
internal fun RiveFit.toIosFit(): nativeIosShared.RiveFit = when (this) {
    RiveFit.FILL -> nativeIosShared.RiveFit.fill
    RiveFit.CONTAIN -> nativeIosShared.RiveFit.contain
    RiveFit.COVER -> nativeIosShared.RiveFit.cover
    RiveFit.FIT_WIDTH -> nativeIosShared.RiveFit.fitWidth
    RiveFit.FIT_HEIGHT -> nativeIosShared.RiveFit.fitHeight
    RiveFit.NONE -> nativeIosShared.RiveFit.noFit
}