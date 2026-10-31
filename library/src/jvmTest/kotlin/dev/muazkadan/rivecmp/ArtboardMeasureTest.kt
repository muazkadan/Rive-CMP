package dev.muazkadan.rivecmp

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import dev.muazkadan.rivecmp.core.RiveFit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks that the desktop and iOS views size themselves like rive-android's RiveAnimationView: the artboard's
 * own size when unconstrained, fitted into the available space when bounded, and the exact size
 * when the constraints are fixed.
 */
class ArtboardMeasureTest {

    private val square = ArtboardSize(width = 500f, height = 500f)
    private val landscape = ArtboardSize(width = 1000f, height = 650f)

    @Test
    fun unconstrainedTakesTheArtboardSize() {
        assertEquals(IntSize(1000, 650), measureArtboard(Constraints(), landscape, RiveFit.CONTAIN))
    }

    @Test
    fun boundedContainScalesDownToFitWhileKeepingAspectRatio() {
        assertEquals(IntSize(300, 300), measureArtboard(Constraints(maxWidth = 300, maxHeight = 1000), square, RiveFit.CONTAIN))
    }

    @Test
    fun boundedContainScalesUpToFillTheAvailableSpace() {
        assertEquals(IntSize(1000, 1000), measureArtboard(Constraints(maxWidth = 1000, maxHeight = 2000), square, RiveFit.CONTAIN))
    }

    @Test
    fun boundedWidthWithUnboundedHeightFollowsTheWidth() {
        assertEquals(IntSize(400, 260), measureArtboard(Constraints(maxWidth = 400), landscape, RiveFit.CONTAIN))
    }

    @Test
    fun fixedConstraintsWin() {
        assertEquals(IntSize(200, 100), measureArtboard(Constraints.fixed(200, 100), square, RiveFit.CONTAIN))
    }

    @Test
    fun fillTakesAllTheAvailableSpace() {
        assertEquals(IntSize(300, 700), measureArtboard(Constraints(maxWidth = 300, maxHeight = 700), square, RiveFit.FILL))
    }

    @Test
    fun noneKeepsTheArtboardSizeWithinTheAvailableSpace() {
        assertEquals(IntSize(300, 500), measureArtboard(Constraints(maxWidth = 300, maxHeight = 700), square, RiveFit.NONE))
    }

    @Test
    fun unconstrainedTakesTheArtboardSizeInDp() {
        assertEquals(IntSize(2000, 1300), measureArtboard(Constraints(), landscape, RiveFit.CONTAIN, density = 2f))
    }

    @Test
    fun boundedWidthWithUnboundedHeightScalesUpToTheArtboardSizeInDp() {
        assertEquals(IntSize(1000, 1000), measureArtboard(Constraints(maxWidth = 2000), square, RiveFit.CONTAIN, density = 2f))
    }

    @Test
    fun noneDrawsOneUnitPerPixelWhateverTheDensity() {
        assertEquals(IntSize(500, 500), measureArtboard(Constraints(), square, RiveFit.NONE, density = 2f))
    }

    @Test
    fun minimumConstraintsAreRespected() {
        assertEquals(IntSize(600, 600), measureArtboard(Constraints(minWidth = 600, minHeight = 600), ArtboardSize(100f, 100f), RiveFit.CONTAIN))
    }

    @Test
    fun emptyArtboardMeasuresToTheMinimum() {
        assertEquals(IntSize(0, 0), measureArtboard(Constraints(maxWidth = 300, maxHeight = 300), ArtboardSize(0f, 0f), RiveFit.CONTAIN))
    }
}
