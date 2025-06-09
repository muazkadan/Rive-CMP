package dev.muazkadan.rivecmpdemo

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.rememberRiveComposition
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import rivecmp.sample.generated.resources.Res
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import dev.materii.pullrefresh.pullRefresh
import dev.muazkadan.rivecmp.core.RiveFit
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Text
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import dev.muazkadan.rivecmp.core.RiveAlignment
import kotlin.math.roundToInt

data class ItemModel(val id: Int, val title: String)

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF001C1C)
        ) {
            CustomPullRefreshSample(height = 200f)
        }
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun CustomPullRefreshSample(height: Float) {
    // Variables
    val refreshScope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { height.dp.toPx() }

    var refreshing by remember { mutableStateOf(false) }
    // var itemCount by remember { mutableStateOf(15) } // Replaced by a list of ItemModels
    val items = remember { mutableStateListOf<ItemModel>() }
    LaunchedEffect(Unit) { // Initialize the list
        if (items.isEmpty()) { // Avoid re-initializing on recompositions if not desired
            items.addAll(generateItemModels())
        }
    }
    var currentDistance by remember { mutableStateOf(0f) }
    val pullToRefreshAnimation by rememberRiveComposition( // Renamed for clarity
        spec = {
            RiveCompositionSpec.byteArray(
                Res.readBytes("files/pull_to_refresh_use_case.riv")
            )
        }
    )

    val progress = currentDistance / threshold

    val scrollValue by animateFloatAsState(
        targetValue = height * progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        finishedListener = { value ->
            if (value == 0f) {
                // Only reset the animation when not visible
                pullToRefreshAnimation?.reset()
                pullToRefreshAnimation?.pause()
            }
        }
    )

    // Methods
    fun refresh() = refreshScope.launch {
        refreshing = true
        // This simulates loading data with delays. The delays are added to ensure the different
        // states of the animation has time to play
        pullToRefreshAnimation?.setTriggerInput("numberSimulation", "advance")
        delay(1500) // Some future to complete - loading data for example
        pullToRefreshAnimation?.setTriggerInput("numberSimulation", "advance")
        delay(1500)

        // Once complete set the target value back to 0, and refreshing false.
        animate(initialValue = currentDistance, targetValue = 0f) { value, _ ->
            currentDistance = value
        }
        refreshing = false
    }

    fun onPull(pullDelta: Float): Float = when {
        refreshing -> 0f
        else -> {
            val newOffset = (currentDistance + pullDelta).coerceAtLeast(0f)
            val dragConsumed = newOffset - currentDistance

            currentDistance = newOffset
            pullToRefreshAnimation?.setNumberInput("numberSimulation", "pull", progress * 100)
            dragConsumed
        }
    }

    fun onRelease(velocity: Float): Float {
        if (refreshing) return 0f // Already refreshing - don't call refresh again.
        var targetValue = 0f;
        if (currentDistance > threshold) {
            targetValue = threshold
            refresh()
        }

        refreshScope.launch {
            animate(initialValue = currentDistance, targetValue = targetValue) { value, _ ->
                currentDistance = value
            }
        }

        // Only consume if the fling is downwards and the indicator is visible
        return if (velocity > 0f && currentDistance > 0f) {
            velocity
        } else {
            0f
        }
    }

    Box(Modifier.pullRefresh(::onPull, ::onRelease)) {
        if (scrollValue > 0) {
            Box(
                Modifier
                    .height(height.dp)
                    .offset(
                        0.dp,
                        (-(height / 2 - scrollValue / 2).coerceIn(
                            maximumValue = height,
                            minimumValue = 0f
                        )).dp
                    )
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                CustomRiveAnimation(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    composition = pullToRefreshAnimation,
                    stateMachineName = "numberSimulation",
                    fit = RiveFit.COVER
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .statusBarsPadding()
                .offset(y = (scrollValue).dp)
                .fillMaxHeight()
                .background(Color(0xFF001C1C)),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            items(
                count = items.size,
                key = { items[it].id }
            ) { index ->
                val item = items[index]
                ListItemUI(model = item, onDelete = {
                    items.remove(it)
                })
            }

        }
    }

}

// Removed SwipeToDeleteRiveAnimation composable as it's no longer needed.
// We will use dev.muazkadan.rivecmp.CustomRiveAnimation directly.

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun ListItemUI(
    model: ItemModel,
    onDelete: (ItemModel) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    // Add separate offset for alligator animation
    var alligatorOffsetX by remember { mutableStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    var isTakingItem by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { 120.dp.toPx() } // Threshold for delete action
    val itemHeight = 100.dp // Standard item height

    // Use the provided Rive composition
    val alligatorComposition by rememberRiveComposition(
        spec = {
            RiveCompositionSpec.byteArray(
                Res.readBytes("files/alligator_swipe.riv")
            )
        }
    )

    val stateMachineName = "State Machine 1" // State machine name from Rive file
    val scrollInputName = "scroll" // Input name for controlling animation
    val maxSwipeOffset = -thresholdPx * 1.3f // Maximum swipe distance

    LaunchedEffect(isTakingItem, isDeleting) {
        if (isTakingItem) {
            // First phase: Alligator takes the item
            alligatorComposition?.setNumberInput(stateMachineName, scrollInputName, 100f)
            delay(400) // Wait for the "bite" part of animation

            // Second phase: Animate in opposite directions
            // Item slides LEFT (-1000) and alligator slides RIGHT (1000)
            val initialItemOffset = offsetX
            val initialAlligatorOffset = alligatorOffsetX

            launch {
                // Animate the item to the left
                animate(
                    initialValue = initialItemOffset,
                    targetValue = -1000f, // Item slides left (off screen)
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    offsetX = value
                }
            }

            launch {
                // Animate the alligator to the right
                animate(
                    initialValue = initialAlligatorOffset,
                    targetValue = -1000f, // Alligator slides right (off screen)
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                ) { value, _ ->
                    alligatorOffsetX = value
                }
            }

            // Wait for animations to finish
            delay(650)

            // Finally delete the item
            isDeleting = true
            onDelete(model)
        }
    }

    LaunchedEffect(offsetX, alligatorComposition) {
        if (!isTakingItem && alligatorComposition != null) {
            // Map swipe distance to animation progress (0-100)
            val scrollPercent = ((-offsetX / thresholdPx) * 100f).coerceIn(0f, 100f) * .99f
            alligatorComposition?.setNumberInput(stateMachineName, scrollInputName, scrollPercent)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX < -thresholdPx && !isTakingItem && !isDeleting) {
                            // Crossed threshold, trigger take animation
                            isTakingItem = true
                        } else if (!isTakingItem && !isDeleting) {
                            // Return to original position with animation
                            scope.launch {
                                animate(offsetX, 0f) { value, _ ->
                                    offsetX = value
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!isTakingItem && !isDeleting) {
                            // Allow dragging left only and limit how far it can be dragged
                            // Restrict swiping to a maximum of maxSwipeOffset
                            offsetX = (offsetX + dragAmount.x).coerceIn(maxSwipeOffset, 0f)
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) } // Apply drag offset
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(itemHeight - 44.dp) // Account for padding
                .background(color = Color(0xFF003C3D), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = model.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxSize()
                .offset(x = (alligatorOffsetX - offsetX - 500).dp, y = (-120).dp)
                .scale(2.5f)
        ) {
            CustomRiveAnimation(
                composition = alligatorComposition,
                stateMachineName = stateMachineName,
                fit = RiveFit.COVER,
                alignment = RiveAlignment.CENTER_LEFT,
                modifier = Modifier.fillMaxSize() // Fill the available space in the Box
            )
        }
    }
}

// Helper function to generate ItemModels
private fun generateItemModels(count: Int = 20): List<ItemModel> { // Increased default count
    return List(count) { ItemModel(id = it, title = "Swipeable Item ${it + 1}") }
}