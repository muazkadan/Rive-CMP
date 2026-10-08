package dev.muazkadan.rivecmp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Lays out and renders a real CustomRiveAnimation without a size modifier: it must take the
 * artboard's fitted size instead of collapsing to 0x0 inside a parent with loose constraints, and
 * the native renderer must draw into it, which also catches a bridge binary that fails on first render.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRiveCmpApi::class)
class CustomRiveAnimationLayoutTest {

    private val bytes = requireNotNull(javaClass.classLoader.getResource("mode_switch.riv")) {
        "Test asset 'mode_switch.riv' not found in resources"
    }.readBytes()

    @Test
    fun fitsTheArtboardIntoALooseParent() = runComposeUiTest {
        RiveDesktop.init()
        // The view requests a frame every frame, so it never goes idle on its own
        mainClock.autoAdvance = false

        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.size(300.dp, 600.dp)) {
                    CustomRiveAnimation(modifier = Modifier.testTag(TAG), byteArray = bytes)
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            mainClock.advanceTimeByFrame()
            onAllNodesWithTag(TAG).fetchSemanticsNodes().isNotEmpty()
        }

        // The 574 x 511 artboard contained in 300 x 600: scaled by 300 / 574
        assertEquals(IntSize(300, 267), onNodeWithTag(TAG).fetchSemanticsNode().size)
    }

    @Test
    fun takesTheArtboardSizeInDpWhenUnconstrained() = runComposeUiTest {
        RiveDesktop.init()
        // The view requests a frame every frame, so it never goes idle on its own
        mainClock.autoAdvance = false

        setContent {
            CompositionLocalProvider(LocalDensity provides Density(2f)) {
                Box(Modifier.wrapContentSize(unbounded = true)) {
                    CustomRiveAnimation(modifier = Modifier.testTag(TAG), byteArray = bytes)
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            mainClock.advanceTimeByFrame()
            onAllNodesWithTag(TAG).fetchSemanticsNodes().isNotEmpty()
        }

        // The 574 x 511 artboard at one dp per unit, two pixels per dp
        assertEquals(IntSize(1148, 1022), onNodeWithTag(TAG).fetchSemanticsNode().size)
    }

    @Test
    fun drawsTheArtboard() = runComposeUiTest {
        RiveDesktop.init()
        // The view requests a frame every frame, so it never goes idle on its own
        mainClock.autoAdvance = false

        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.size(300.dp, 600.dp)) {
                    CustomRiveAnimation(modifier = Modifier.testTag(TAG), byteArray = bytes)
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            mainClock.advanceTimeByFrame()
            onAllNodesWithTag(TAG).fetchSemanticsNodes().isNotEmpty()
        }
        repeat(10) { mainClock.advanceTimeByFrame() }

        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val drawn = (0 until pixels.width).any { x -> (0 until pixels.height).any { y -> pixels[x, y].alpha > 0f } }
        assertTrue(drawn, "Expected the artboard to draw at least one visible pixel")
    }

    private companion object {
        const val TAG = "rive"
    }
}
