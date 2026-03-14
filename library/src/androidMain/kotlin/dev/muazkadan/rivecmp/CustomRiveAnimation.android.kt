package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import android.util.LruCache
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import app.rive.RiveUI
import app.rive.ViewModelInstance
import app.rive.rememberArtboard
import app.rive.ExperimentalRiveComposeAPI
import app.rive.rememberViewModelInstance
import app.rive.RiveFileSource
import app.rive.rememberRiveFile
import app.rive.Result
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toAndroidAlignment
import dev.muazkadan.rivecmp.core.toAndroidFit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean
) {
    if (composition != null) {
        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                CustomRiveAnimationInternal(
                    modifier = modifier,
                    url = spec.url,
                    alignment = alignment,
                    autoPlay = autoPlay,
                    artboardName = artboardName,
                    fit = fit,
                    stateMachineName = stateMachineName,
                    overlay = overlay,
                    onViewModelInstanceReady = { vmi ->
                        composition.connectToAnimationView(vmi)
                    }
                )
            }
            is RiveByteArrayCompositionSpec -> {
                 CustomRiveAnimationInternal(
                    modifier = modifier,
                    byteArray = spec.byteArray,
                    alignment = alignment,
                    autoPlay = autoPlay,
                    artboardName = artboardName,
                    fit = fit,
                    stateMachineName = stateMachineName,
                    overlay = overlay,
                    onViewModelInstanceReady = { vmi ->
                        composition.connectToAnimationView(vmi)
                    }
                )
            }
        }
    }
}

@ExperimentalRiveCmpApi
private val riveByteCache = LruCache<String, ByteArray>(20)

@ExperimentalRiveCmpApi
@Composable
internal fun CustomRiveAnimationInternal(
    modifier: Modifier,
    url: String,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean,
    onViewModelInstanceReady: ((ViewModelInstance?) -> Unit)?
) {
    val bytes = produceState<ByteArray?>(initialValue = riveByteCache.get(url), key1 = url) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                try {
                    val downloaded = URL(url).readBytes()
                    riveByteCache.put(url, downloaded)
                    downloaded
                } catch (e: Exception) {
                    null
                }
            }
        }
    }.value

    if (bytes != null) {
        CustomRiveAnimationInternal(
            modifier = modifier,
            byteArray = bytes,
            alignment = alignment,
            autoPlay = autoPlay,
            artboardName = artboardName,
            fit = fit,
            stateMachineName = stateMachineName,
            overlay = overlay,
            onViewModelInstanceReady = onViewModelInstanceReady
        )
    }
}

@ExperimentalRiveCmpApi
@Composable
internal fun CustomRiveAnimationInternal(
    modifier: Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    overlay: Boolean,
    onViewModelInstanceReady: ((ViewModelInstance?) -> Unit)?
) {
    val source: RiveFileSource = RiveFileSource.Bytes(byteArray)
    val riveFile = rememberRiveFile(
        source = source
    )

    when (val result = riveFile.value) {
        is Result.Success -> {
            val actualFile = result.value
            val artboard = rememberArtboard(actualFile, artboardName)
            
            val vmi = if (onViewModelInstanceReady != null) {
                val vmSource = app.rive.ViewModelSource.DefaultForArtboard(artboard).defaultInstance()
                rememberViewModelInstance(actualFile, vmSource)
            } else null
            
            LaunchedEffect(vmi) {
                onViewModelInstanceReady?.invoke(vmi)
            }

            // RiveUI expects the runtime Fit and Alignment
            RiveUI(
                file = actualFile,
                modifier = modifier,
                artboard = artboard,
                stateMachineName = stateMachineName,
                viewModelInstance = vmi,
                fit = fit.toAndroidFit(),
                alignment = alignment.toAndroidAlignment()
            )
        }
        else -> {
            // Wait while loading or handle failure silently 
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
    stateMachineName: String?,
    overlay: Boolean
) {
    CustomRiveAnimationInternal(
        modifier = modifier,
        url = url,
        alignment = alignment,
        autoPlay = autoPlay,
        artboardName = artboardName,
        fit = fit,
        stateMachineName = stateMachineName,
        overlay = overlay,
        onViewModelInstanceReady = null
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
    stateMachineName: String?,
    overlay: Boolean
) {
    CustomRiveAnimationInternal(
        modifier = modifier,
        byteArray = byteArray,
        alignment = alignment,
        autoPlay = autoPlay,
        artboardName = artboardName,
        fit = fit,
        stateMachineName = stateMachineName,
        overlay = overlay,
        onViewModelInstanceReady = null
    )
}