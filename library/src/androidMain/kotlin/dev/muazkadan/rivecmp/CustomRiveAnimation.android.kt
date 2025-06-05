package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Alignment

@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    stateMachineName: String?
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            RiveAnimationView.Builder(context)
                .setResource(url)
                .setAlignment(Alignment.CENTER)
                .build()
        }
    )
}