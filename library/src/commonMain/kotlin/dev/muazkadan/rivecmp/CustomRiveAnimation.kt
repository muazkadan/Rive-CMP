package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    url: String,
    stateMachineName: String? = null,
)