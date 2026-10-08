package dev.muazkadan.rivecmp

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.native.File
import dev.muazkadan.rivecmp.native.RiveFileController
import dev.muazkadan.rivecmp.native.model.Fit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Binds a real file's default view model through CustomRiveAnimation on desktop and checks what
 * the callback receives, when it runs, and that the instance stays safe to use.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRiveCmpApi::class)
class CustomRiveAnimationDataBindingTest {

    private val bound = resource("data_binding_test_triggers.riv")
    private val unbound = resource("mode_switch.riv")

    private fun resource(name: String): ByteArray =
        requireNotNull(javaClass.classLoader.getResource(name)) {
            "Test asset '$name' not found in resources"
        }.readBytes()

    /** Steps frames until the animation is laid out, then a few more so effects have run. */
    private fun ComposeUiTest.awaitAnimation() {
        waitUntil(timeoutMillis = 5_000) {
            mainClock.advanceTimeByFrame()
            onAllNodesWithTag(TAG).fetchSemanticsNodes().isNotEmpty()
        }
        repeat(5) { mainClock.advanceTimeByFrame() }
    }

    @Test
    fun theCallbackReceivesTheBoundInstance() = runComposeUiTest {
        RiveDesktop.init()
        // The view requests a frame every frame, so it never goes idle on its own
        mainClock.autoAdvance = false
        val received = mutableListOf<RiveViewModelInstance>()

        setContent {
            CustomRiveAnimation(
                modifier = Modifier.size(100.dp).testTag(TAG),
                byteArray = bound,
                onViewModelInstance = { received += it },
            )
        }
        awaitAnimation()

        assertEquals(1, received.size, "Expected exactly one callback")
        val instance = received.single()
        assertNotNull(instance.trigger("trigger"), "Expected vm_root's trigger property")
        assertNull(instance.number("missing"))
        assertNull(instance.number("trigger"), "Expected null for a path of another type")
        assertNull(instance.trigger("missing/trigger"))
    }

    @Test
    fun theGraphicFiringATriggerReachesTheFlow() = runComposeUiTest {
        RiveDesktop.init()
        mainClock.autoAdvance = false
        val received = mutableListOf<RiveViewModelInstance>()
        val scope = CoroutineScope(Dispatchers.Unconfined)
        var firings = 0

        setContent {
            CustomRiveAnimation(
                modifier = Modifier.size(100.dp).testTag(TAG),
                byteArray = bound,
                stateMachineName = "State Machine 1",
                onViewModelInstance = { received += it },
            )
        }
        awaitAnimation()
        val trigger = assertNotNull(received.single().trigger("trigger"))
        scope.launch { trigger.triggers.collect { firings++ } }

        // The graphic fires the trigger about 0.8 s in; 120 frames is about 2 s.
        repeat(120) { mainClock.advanceTimeByFrame() }
        scope.cancel()

        assertTrue(firings >= 1, "Expected the graphic to fire the trigger, got $firings firings")
    }

    @Test
    fun firingFromCodeAfterTheGraphicWentIdleReachesTheFlow() = runComposeUiTest {
        RiveDesktop.init()
        mainClock.autoAdvance = false
        val received = mutableListOf<RiveViewModelInstance>()
        val scope = CoroutineScope(Dispatchers.Unconfined)
        var firings = 0

        setContent {
            CustomRiveAnimation(
                modifier = Modifier.size(100.dp).testTag(TAG),
                byteArray = bound,
                stateMachineName = "State Machine 1",
                onViewModelInstance = { received += it },
            )
        }
        awaitAnimation()
        val trigger = assertNotNull(received.single().trigger("trigger"))
        scope.launch { trigger.triggers.collect { firings++ } }
        // About 10 s of frames: the graphic fires once by itself and then stops animating.
        repeat(600) { mainClock.advanceTimeByFrame() }
        val firingsWhileIdle = firings

        trigger.trigger()
        repeat(5) { mainClock.advanceTimeByFrame() }
        scope.cancel()

        assertEquals(firingsWhileIdle + 1, firings, "Expected the trigger fired while idle to reach the flow")
    }

    @Test
    fun aFileWithoutAViewModelPlaysAndNeverCallsBack() = runComposeUiTest {
        RiveDesktop.init()
        mainClock.autoAdvance = false
        var calls = 0

        setContent {
            CustomRiveAnimation(
                modifier = Modifier.size(100.dp).testTag(TAG),
                byteArray = unbound,
                onViewModelInstance = { calls++ },
            )
        }
        awaitAnimation()
        repeat(30) { mainClock.advanceTimeByFrame() }

        assertEquals(0, calls)
    }

    @Test
    fun aNewCallbackEachRecompositionDoesNotRebind() = runComposeUiTest {
        RiveDesktop.init()
        mainClock.autoAdvance = false
        var calls = 0
        var recompositions by mutableIntStateOf(0)

        setContent {
            val tick = recompositions
            CustomRiveAnimation(
                modifier = Modifier.size(100.dp).testTag(TAG),
                byteArray = bound,
                // A new lambda instance on every recomposition, because it captures tick
                onViewModelInstance = { calls += 1 + tick * 0 },
            )
        }
        awaitAnimation()
        assertEquals(1, calls)

        repeat(3) {
            recompositions++
            repeat(3) { mainClock.advanceTimeByFrame() }
        }

        assertEquals(1, calls, "Expected the callback not to run again for a new lambda")
    }

    @Test
    fun aPropertyOutlivingItsAnimationDoesNotCrash() = runComposeUiTest {
        RiveDesktop.init()
        mainClock.autoAdvance = false
        val received = mutableListOf<RiveViewModelInstance>()
        var shown by mutableStateOf(true)

        setContent {
            if (shown) {
                CustomRiveAnimation(
                    modifier = Modifier.size(100.dp).testTag(TAG),
                    byteArray = bound,
                    onViewModelInstance = { received += it },
                )
            }
        }
        awaitAnimation()
        val trigger = assertNotNull(received.single().trigger("trigger"))

        shown = false
        mainClock.advanceTimeByFrame()
        waitForIdle()

        trigger.trigger()
        assertNull(received.single().number("missing"))
    }

    @Test
    fun writingAPropertyResumesASettledStateMachine() {
        RiveDesktop.init()
        val file = File(bound)
        try {
            val controller = RiveFileController(
                autoplay = true,
                autoBind = true,
                stateMachineName = "State Machine 1",
                file = file,
                fit = Fit.CONTAIN,
            )
            val instance = DesktopRiveViewModelInstance(
                assertNotNull(controller.viewModelInstance),
                controller::resumeStateMachines,
            )
            // Advance well past the point where the state machine stops animating.
            repeat(600) { controller.advance(1f / 60) }
            assertFalse(controller.isAdvancing, "Expected the state machine to have settled")

            assertNotNull(instance.trigger("trigger")).trigger()

            assertTrue(controller.isAdvancing, "Expected a write to resume the state machine")
            controller.dispose()
        } finally {
            file.release()
        }
    }

    @Test
    fun resettingTheCompositionCallsBackWithAFreshInstance() = runComposeUiTest {
        RiveDesktop.init()
        mainClock.autoAdvance = false
        val received = mutableListOf<RiveViewModelInstance>()
        var composition: RiveComposition? = null
        val scope = CoroutineScope(Dispatchers.Unconfined)
        var firings = 0

        setContent {
            val loaded by rememberRiveComposition { RiveCompositionSpec.byteArray(bound) }
            composition = loaded
            CustomRiveAnimation(
                modifier = Modifier.size(100.dp).testTag(TAG),
                composition = loaded,
                stateMachineName = "State Machine 1",
                onViewModelInstance = { received += it },
            )
        }
        awaitAnimation()
        assertEquals(1, received.size)

        assertNotNull(composition).reset()
        repeat(5) { mainClock.advanceTimeByFrame() }

        assertEquals(2, received.size, "Expected reset to call back again")
        assertNull(received.first().trigger("trigger"), "Expected the replaced instance to be released")
        val trigger = assertNotNull(received.last().trigger("trigger"))
        scope.launch { trigger.triggers.collect { firings++ } }
        trigger.trigger()
        repeat(5) { mainClock.advanceTimeByFrame() }
        scope.cancel()
        assertTrue(firings >= 1, "Expected the instance bound after reset to be live, got $firings firings")
    }

    @Test
    fun aTriggerFiredWhileIdleReachesTheFlow() {
        RiveDesktop.init()
        val file = File(bound)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        try {
            val controller = RiveFileController(
                autoplay = true,
                autoBind = true,
                stateMachineName = "State Machine 1",
                file = file,
                fit = Fit.CONTAIN,
            )
            val instance = DesktopRiveViewModelInstance(
                assertNotNull(controller.viewModelInstance),
                controller::resumeStateMachines,
            )
            val trigger = assertNotNull(instance.trigger("trigger"))
            var firings = 0
            scope.launch { trigger.triggers.collect { firings++ } }
            // Advance well past the point where the state machine stops animating.
            repeat(600) { controller.advance(1f / 60) }
            assertFalse(controller.isAdvancing, "Expected the state machine to have settled")
            val firingsWhileIdle = firings

            trigger.trigger()
            repeat(5) { controller.advance(1f / 60) }

            assertEquals(firingsWhileIdle + 1, firings, "Expected the trigger fired while idle to reach the flow")
            controller.dispose()
        } finally {
            scope.cancel()
            file.release()
        }
    }

    @Test
    fun aStateMachineRecreatedAfterStopStaysBound() {
        RiveDesktop.init()
        val file = File(bound)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        try {
            val controller = RiveFileController(
                autoplay = true,
                autoBind = true,
                stateMachineName = "State Machine 1",
                file = file,
                fit = Fit.CONTAIN,
            )
            val instance = DesktopRiveViewModelInstance(
                assertNotNull(controller.viewModelInstance),
                controller::resumeStateMachines,
            )
            val trigger = assertNotNull(instance.trigger("trigger"))
            var firings = 0
            scope.launch { trigger.triggers.collect { firings++ } }

            controller.stopAnimations()
            controller.play("State Machine 1", isStateMachine = true)
            repeat(5) { controller.advance(1f / 60) }
            val firingsBefore = firings
            trigger.trigger()
            repeat(5) { controller.advance(1f / 60) }

            assertTrue(firings > firingsBefore, "Expected a trigger fired after stop and play to reach the flow")
            controller.dispose()
        } finally {
            scope.cancel()
            file.release()
        }
    }

    private companion object {
        const val TAG = "rive"
    }
}
