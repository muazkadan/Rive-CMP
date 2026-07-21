package dev.muazkadan.rivecmp.core

import dev.muazkadan.rivecmp.native.model.Fit

/**
 * Maps common RiveFit to the JVM native bridge's Fit.
 *
 * Mapped by name, not ordinal: the native `Fit` enum has an extra `LAYOUT` member (used
 * internally by the JNI bridge) that [RiveFit] doesn't expose, so the two enums' ordinals don't
 * line up - relying on `.ordinal` here would silently map [RiveFit.NONE] onto `Fit.LAYOUT`.
 */
internal fun RiveFit.toJvmFit(): Fit = when (this) {
    RiveFit.FILL -> Fit.FILL
    RiveFit.CONTAIN -> Fit.CONTAIN
    RiveFit.COVER -> Fit.COVER
    RiveFit.FIT_WIDTH -> Fit.FIT_WIDTH
    RiveFit.FIT_HEIGHT -> Fit.FIT_HEIGHT
    RiveFit.NONE -> Fit.NONE
}
