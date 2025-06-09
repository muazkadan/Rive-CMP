package dev.muazkadan.rivecmpdemo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.isSystemInDarkTheme
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
    var itemCount by remember { mutableStateOf(15) }
    var currentDistance by remember { mutableStateOf(0f) }
    val animation by rememberRiveComposition(
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
                animation?.reset()
                animation?.pause()
            }
        }
    )

    // Methods
    fun refresh() = refreshScope.launch {
        refreshing = true
        // This simulates loading data with delays. The delays are added to ensure the different
        // states of the animation has time to play
        animation?.setTriggerInput("numberSimulation", "advance")
        delay(1500) // Some future to complete - loading data for example
        animation?.setTriggerInput("numberSimulation", "advance")
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
            animation?.setNumberInput("numberSimulation", "pull", progress * 100)
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
                    composition = animation,
                    stateMachineName = "numberSimulation",
                    fit = RiveFit.COVER
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .offset(y = (scrollValue).dp)
                .fillMaxHeight()
                .background(Color(0xFF001C1C))
        ) {
            items(itemCount) {
                ListItemUI()
            }

        }
    }

}

@Composable
fun ListItemUI() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(100.dp)
            .background(color = Color(0xFF003C3D), shape = RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterStart)
                .size(64.dp)
                .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
        )

        Box(
            modifier = Modifier
                .padding(start = 88.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
                .align(alignment = Alignment.Center)
                .fillMaxWidth()
                .height(16.dp)
                .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
        )

        Box(
            modifier = Modifier
                .padding(start = 88.dp, top = 40.dp, end = 16.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(16.dp)
                .background(color = Color.DarkGray, shape = RoundedCornerShape(4.dp))
        )
    }
}