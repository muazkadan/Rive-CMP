package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toAndroidFit
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.toAndroidAlignment
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {
    if (composition != null) {
        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                AndroidView(
                    modifier = modifier,
                    factory = { context ->
                        val builder = RiveAnimationView.Builder(context)
                            .setResource(spec.url)
                            .setAlignment(alignment.toAndroidAlignment())
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
                    },
                    update = { view ->
                        composition.connectToAnimationView(view)
                    }
                )
            }
            is RiveByteArrayCompositionSpec -> {
                AndroidView(
                    modifier = modifier,
                    factory = { context ->
                        val builder = RiveAnimationView.Builder(context)
                            .setResource(spec.byteArray)
                            .setAlignment(alignment.toAndroidAlignment())
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
                    },
                    update = { view ->
                        composition.connectToAnimationView(view)
                    }
                )
            }
        }
    }
}

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    alignment: RiveAlignment,
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
                .setAlignment(alignment.toAndroidAlignment())
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

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val builder = RiveAnimationView.Builder(context)
                .setResource(byteArray)
                .setAlignment(alignment.toAndroidAlignment())
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