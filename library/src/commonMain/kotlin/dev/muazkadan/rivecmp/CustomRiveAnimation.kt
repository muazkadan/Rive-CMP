package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

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
)