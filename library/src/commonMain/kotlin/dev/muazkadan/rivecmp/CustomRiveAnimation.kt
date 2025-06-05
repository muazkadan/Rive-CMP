package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    url: String,
    stateMachineName: String? = null,
)