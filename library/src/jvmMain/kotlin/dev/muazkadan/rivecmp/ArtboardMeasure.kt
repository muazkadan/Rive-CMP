/**
 * Measurement of the desktop Rive view. It follows rive-android's RiveAnimationView.onMeasure so a
 * CustomRiveAnimation without a size modifier lays out the same on desktop as on Android: the
 * artboard's own size in unbounded dimensions, fitted into the space the parent allows otherwise.
 */
package dev.muazkadan.rivecmp

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import dev.muazkadan.rivecmp.native.model.Fit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** An artboard's width and height in artboard units, one unit per pixel. */
internal data class ArtboardSize(val width: Float, val height: Float)

/**
 * The size the view takes for [artboard] under [constraints]. Each bounded dimension offers its
 * maximum and each unbounded one the artboard's own extent; the artboard is scaled into that space
 * by [fit], and the result is clamped to [constraints].
 */
internal fun measureArtboard(
    constraints: Constraints,
    artboard: ArtboardSize,
    fit: Fit,
    scaleFactor: Float = 1f,
): IntSize {
    val availableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() else artboard.width
    val availableHeight = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() else artboard.height

    val required = requiredSize(fit, availableWidth, availableHeight, artboard, scaleFactor)

    return IntSize(
        width = constraints.constrainWidth(required.width.roundToInt()),
        height = constraints.constrainHeight(required.height.roundToInt()),
    )
}

/** The space [artboard] covers once scaled into the available space, as rive-runtime's computeAlignment scales it. */
private fun requiredSize(
    fit: Fit,
    availableWidth: Float,
    availableHeight: Float,
    artboard: ArtboardSize,
    scaleFactor: Float,
): ArtboardSize {
    if (artboard.width <= 0f || artboard.height <= 0f) return ArtboardSize(0f, 0f)

    val widthScale = availableWidth / artboard.width
    val heightScale = availableHeight / artboard.height

    val scale = when (fit) {
        Fit.FILL -> return ArtboardSize(availableWidth, availableHeight)
        Fit.CONTAIN -> min(widthScale, heightScale)
        Fit.COVER -> max(widthScale, heightScale)
        Fit.FIT_WIDTH -> widthScale
        Fit.FIT_HEIGHT -> heightScale
        Fit.LAYOUT -> scaleFactor
        Fit.NONE -> 1f
    }

    return ArtboardSize(artboard.width * scale, artboard.height * scale)
}
