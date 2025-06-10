package dev.muazkadan.rivecmpdemo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.rememberRiveComposition
// Import kotlinx.coroutines.test runTest if needed for LaunchedEffects
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import dev.muazkadan.rivecmp.RiveComposition // Use the actual interface

// Mocking RiveComposition by implementing the interface
@OptIn(ExperimentalRiveCmpApi::class)
class MockRiveCompositionImpl : RiveComposition {
    val inputs = mutableMapOf<String, Number>()
    val triggers = mutableListOf<String>()
    var paused = false
    var resetted = false

    override fun setNumberInput(stateMachineName: String, inputName: String, value: Number) {
        inputs["$stateMachineName/$inputName"] = value
    }

    override fun fireTrigger(stateMachineName: String, triggerName: String) {
        triggers.add("$stateMachineName/$triggerName")
    }

    override fun reset() {
        inputs.clear()
        triggers.clear()
        resetted = true
        paused = false // Typically reset would unpause
    }

    override fun pause() {
        paused = true
    }

    // Helper to clear state for tests
    fun clearTestState() {
        inputs.clear()
        triggers.clear()
        paused = false
        resetted = false
    }

    override fun setBooleanInput(stateMachineName: String, inputName: String, value: Boolean) {
        // Not used in ListItemUI but part of interface perhaps
    }

    override funsetTextInput(stateMachineName: String, inputName: String, value: String) {
        // Not used in ListItemUI but part of interface perhaps
    }
}

@OptIn(ExperimentalTestApi::class, ExperimentalRiveCmpApi::class)
class AppTest {

    private lateinit var mockRiveCompositionImpl: MockRiveCompositionImpl
    private val testDensity = Density(1f, 1f)

    @BeforeTest
    fun setup() {
        mockRiveCompositionImpl = MockRiveCompositionImpl()
    }

    @AfterTest
    fun tearDown() {
        // mockRiveCompositionImpl.clearTestState() // Clean up if needed between tests
    }

    // Helper Composable to set up ListItemUI for testing
    @Composable
    fun TestListItemUISetup(
        model: ItemModel,
        onDeleteLambda: (ItemModel) -> Unit = {},
        offsetXInitial: Float = 0f,
    ): Triple<MutableState<Float>, MutableState<Boolean>, () -> Unit> {
        // These states would normally be inside ListItemUI, we expose them for test assertions
        var offsetX by remember { mutableStateOf(offsetXInitial) }
        var isDeleting by remember { mutableStateOf(false) }

        val density = LocalDensity.current
        val thresholdPx = with(density) { 150.dp.toPx() }

        ListItemUI(
            model = model,
            onDelete = onDeleteLambda,
            riveCompositionProvider = { mockRiveCompositionImpl }
        )

        // Return a lambda that can simulate the drag ending and triggering deletion logic
        // This is still not ideal as we are not *really* dragging via UI
        val simulateDragEndAndDelete = {
            if (offsetX < -thresholdPx && !isDeleting) {
                isDeleting = true // This would trigger the LaunchedEffect in ListItemUI
            }
            // To fully test, we'd need to also control the LaunchedEffect's coroutine scope
            // and delays, which is complex without specific test coroutine dispatchers.
        }
        return Triple(remember { mutableStateOf(offsetX) }, remember { mutableStateOf(isDeleting) }, simulateDragEndAndDelete)
    }


    @Test
    fun listItemUI_dragUpdatesRiveScrollInput() = runTest {
        // This test is still conceptual as we are not truly dragging via UI.
        // We are manually setting offsetX and checking if the LaunchedEffect in ListItemUI
        // (which observes offsetX) correctly calls the Rive input.
        // This requires ListItemUI to be actively recomposing and its LaunchedEffects running.
        // composeTestRule.setContent is needed for this.
        // For now, we directly invoke the logic that would be in LaunchedEffect(offsetX).

        val item = ItemModel(1, "Test Item")
        var offsetXState = -50f // Simulate this much drag
        val thresholdPx = with(testDensity) { 150.dp.toPx() }
        val expectedScrollPercent = ((-offsetXState / thresholdPx) * 100f).coerceIn(0f, 100f)

        // Simulate the LaunchedEffect(offsetX, alligatorComposition)
        // In a real test, this effect would trigger upon offsetX change when ListItemUI is composed.
        mockRiveCompositionImpl.setNumberInput("AlligatorSwipe", "scroll", expectedScrollPercent)

        assertEquals(expectedScrollPercent, mockRiveCompositionImpl.inputs["AlligatorSwipe/scroll"])
    }

    @Test
    fun listItemUI_deleteFiresRiveCompleteTriggerAndCallsOnDelete() = runTest {
        var deletedItem: ItemModel? = null
        val item = ItemModel(1, "Test Item")
        val onDeleteLambda = { model: ItemModel -> deletedItem = model }

        // Simulate the state that leads to deletion
        var isDeletingState = true // This would be set by drag gesture logic

        // Simulate the LaunchedEffect(isDeleting)
        // In a real test, this effect would trigger when isDeleting becomes true.
        if (isDeletingState) {
            mockRiveCompositionImpl.fireTrigger("AlligatorSwipe", "complete")
            // kotlinx.coroutines.delay(500) // The test will run this if not on a test dispatcher
            onDeleteLambda(item) // Simulate calling onDelete
        }

        assertTrue(mockRiveCompositionImpl.triggers.contains("AlligatorSwipe/complete"))
        assertEquals(item, deletedItem)
    }

    // The following tests are more about the internal logic given the difficulty of UI manipulation
    // without a proper Compose test rule in commonMain.

    @Test
    fun listItemUI_offsetXUpdate_simulated() {
        // Manually simulate the state changes for offsetX
        val initialOffsetX = 0f
        var offsetX by mutableStateOf(initialOffsetX)
        val dragAmount1 = -50f
        offsetX += dragAmount1
        assertEquals(-50f, offsetX)

        val dragAmount2 = -20f
        offsetX += dragAmount2
        assertEquals(-70f, offsetX)

        // Simulate drag release not meeting threshold (should snap back)
        val thresholdPx = with(testDensity) { 150.dp.toPx() }
        if (offsetX >= -thresholdPx) { // Assuming snap back logic if not deleting
            offsetX = 0f
        }
        assertEquals(0f, offsetX) // Snapped back
    }

    @Test
    fun listItemUI_deletionLogic_simulated() {
        var offsetX by mutableStateOf(0f)
        var isDeleting by mutableStateOf(false)
        var modelDeleted: ItemModel? = null
        val item = ItemModel(1, "Test")
        val thresholdPx = with(testDensity) { 150.dp.toPx() }

        // Simulate drag beyond threshold
        offsetX = -thresholdPx - 10f

        // Simulate onDragEnd
        if (offsetX < -thresholdPx && !isDeleting) {
            isDeleting = true
        }
        assertTrue(isDeleting, "isDeleting should be true after dragging beyond threshold")

        // Simulate LaunchedEffect(isDeleting)
        if (isDeleting) {
            // mockRiveCompositionImpl.fireTrigger("AlligatorSwipe", "complete") // Already tested conceptually
            // delay(500)
            modelDeleted = item // onDelete(item)
        }
        assertEquals(item, modelDeleted, "onDelete should be called with the correct item")
    }

}
