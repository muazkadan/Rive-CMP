package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Alignment
import app.rive.runtime.kotlin.core.Fit
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toAndroidFit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val builder = RiveAnimationView.Builder(context)
                .setResource(url)
                .setAlignment(Alignment.CENTER)
                .setFit(fit.toAndroidFit())
                .setAutoplay(autoPlay)

            // Set artboard name if provided
            artboardName?.let {
                builder.setArtboardName(it)
            }

            // Set state machine name if provided
            stateMachineName?.let {
                builder.setStateMachineName(it)
            }

            builder.build()
        }
    )
}