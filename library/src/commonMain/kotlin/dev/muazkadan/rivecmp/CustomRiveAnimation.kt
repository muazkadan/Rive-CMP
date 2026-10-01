package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

/**
 * @param onViewModelInstance When set, the artboard's default view model instance is bound to the
 * artboard and its state machine, and this is called on the main thread with it once the file has
 * loaded. It is not called for a file without a view model. It is called again with a new instance
 * whenever the binding is replaced: after [RiveComposition.reset], on iOS also after
 * [RiveComposition.stop], and when the native view is recreated. Properties taken from an earlier
 * instance then stop updating. Changing it between `null` and non-null may recreate the native view.
 */
@ExperimentalRiveCmpApi
@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment = RiveAlignment.CENTER,
    autoPlay: Boolean = true,
    artboardName: String? = null,
    fit: RiveFit = RiveFit.CONTAIN,
    stateMachineName: String? = null,
    overlay: Boolean = true,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)? = null,
)

@ExperimentalRiveCmpApi
@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    url: String,
    alignment: RiveAlignment = RiveAlignment.CENTER,
    autoPlay: Boolean = true,
    artboardName: String? = null,
    fit: RiveFit = RiveFit.CONTAIN,
    stateMachineName: String? = null,
    overlay: Boolean = true,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)? = null,
)

@ExperimentalRiveCmpApi
@Composable
expect fun CustomRiveAnimation(
    modifier: Modifier = Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment = RiveAlignment.CENTER,
    autoPlay: Boolean = true,
    artboardName: String? = null,
    fit: RiveFit = RiveFit.CONTAIN,
    stateMachineName: String? = null,
    overlay: Boolean = true,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)? = null,
)