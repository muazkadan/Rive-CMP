package dev.muazkadan.rivecmpdemo

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.materii.pullrefresh.pullRefresh
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.rememberRiveComposition
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import rivecmp.sample.generated.resources.Res
import kotlin.math.roundToInt

data class ItemModel(val id: Int, val title: String)

// Constants for readability and maintainability
private const val ITEM_HEIGHT = 100f
private const val SWIPE_THRESHOLD_DP = 120f
private const val MAX_SWIPE_OFFSET_FACTOR = 1.3f

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
fun CustomPullRefreshSample(
    height: Float,
    modifier: Modifier = Modifier
) {
    val refreshScope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { height.dp.toPx() }

    var refreshing by remember { mutableStateOf(false) }
    val items = remember { mutableStateListOf<ItemModel>() }
    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            items.addAll(generateItemModels())
        }
    }
    var currentDistance by remember { mutableStateOf(0f) }
    val pullToRefreshAnimation by rememberRiveComposition(
        spec = { RiveCompositionSpec.byteArray(Res.readBytes("files/pull_to_refresh_use_case.riv")) }
    )

    val progress = currentDistance / threshold
    val scrollValue by animateFloatAsState(
        targetValue = height * progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pullRefreshAnimation"
    ) { value ->
        if (value == 0f) {
            pullToRefreshAnimation?.reset()
            pullToRefreshAnimation?.pause()
        }
    }

    // Refresh logic
    fun refresh() = refreshScope.launch {
        refreshing = true
        pullToRefreshAnimation?.setTriggerInput("numberSimulation", "advance")
        delay(1500)
        pullToRefreshAnimation?.setTriggerInput("numberSimulation", "advance")
        delay(1500)
        animate(initialValue = currentDistance, targetValue = 0f) { value, _ ->
            currentDistance = value
        }
        refreshing = false
    }

    // Pull-to-refresh gesture handling
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
        if (refreshing) return 0f
        val targetValue = if (currentDistance > threshold) threshold else 0f
        if (targetValue > 0f) refresh()
        refreshScope.launch {
            animate(initialValue = currentDistance, targetValue = targetValue) { value, _ ->
                currentDistance = value
            }
        }
        return if (velocity > 0f && currentDistance > 0f) velocity else 0f
    }

    Box(modifier.pullRefresh(::onPull, ::onRelease)) {
        if (scrollValue > 0) {
            Box(
                modifier = Modifier
                    .height(height.dp)
                    .offset(y = (-(height / 2 - scrollValue / 2).coerceIn(0f, height)).dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                CustomRiveAnimation(
                    modifier = Modifier.fillMaxWidth().height(height.dp),
                    composition = pullToRefreshAnimation,
                    stateMachineName = "numberSimulation",
                    fit = RiveFit.COVER
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .statusBarsPadding()
                .offset(y = scrollValue.dp)
                .fillMaxHeight()
                .background(Color(0xFF001C1C)),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ListItemUI(
                    modifier = Modifier.animateItem(),
                    model = item,
                    onDelete = { items.remove(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun ListItemUI(
    modifier: Modifier = Modifier,
    model: ItemModel,
    onDelete: (ItemModel) -> Unit
) {
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    var alligatorOffsetX by remember { mutableStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    var isTakingItem by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }
    val maxSwipeOffset = -thresholdPx * MAX_SWIPE_OFFSET_FACTOR

    val alligatorComposition by rememberRiveComposition(
        spec = { RiveCompositionSpec.byteArray(Res.readBytes("files/alligator_swipe.riv")) }
    )

    LaunchedEffect(isTakingItem, isDeleting) {
        if (isTakingItem) {
            alligatorComposition?.setNumberInput("State Machine 1", "scroll", 100f)
            delay(400)
            launch {
                animate(
                    initialValue = offsetX,
                    targetValue = -1000f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                ) { value, _ -> offsetX = value }
            }
            launch {
                animate(
                    initialValue = alligatorOffsetX,
                    targetValue = -1000f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                ) { value, _ -> alligatorOffsetX = value }
            }
            delay(650)
            isDeleting = true
            onDelete(model)
        }
    }

    LaunchedEffect(offsetX, alligatorComposition) {
        if (!isTakingItem && !isDeleting && alligatorComposition != null) {
            val scrollPercent = ((-offsetX / thresholdPx) * 100f).coerceIn(0f, 100f) * 0.99f
            alligatorComposition?.setNumberInput("State Machine 1", "scroll", scrollPercent)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ITEM_HEIGHT.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < -thresholdPx && !isTakingItem && !isDeleting) {
                            isTakingItem = true
                        } else if (!isTakingItem && !isDeleting) {
                            scope.launch {
                                animate(offsetX, 0f) { value, _ -> offsetX = value }
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (!isTakingItem && !isDeleting) {
                            val newOffset = (offsetX + dragAmount).coerceIn(maxSwipeOffset, 0f)
                            if (newOffset != offsetX) {
                                change.consume()
                                offsetX = newOffset
                            }
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(ITEM_HEIGHT.dp - 44.dp)
                .background(Color(0xFF003C3D), RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = model.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (offsetX < 0f || isTakingItem) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize()
                    .offset(x = (alligatorOffsetX - offsetX - 500).dp, y = (-120).dp)
                    .scale(2.5f)
            ) {
                CustomRiveAnimation(
                    modifier = Modifier.fillMaxSize(),
                    composition = alligatorComposition,
                    stateMachineName = "State Machine 1",
                    fit = RiveFit.COVER,
                    alignment = RiveAlignment.CENTER_LEFT
                )
            }
        }
    }
}

private fun generateItemModels(count: Int = 20): List<ItemModel> {
    return List(count) { ItemModel(id = it, title = "Swipeable Item ${it + 1}") }
}