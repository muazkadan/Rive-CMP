package dev.muazkadan.rivecmpdemo

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
import org.jetbrains.compose.ui.tooling.preview.Preview
import rivecmp.sample.generated.resources.Res

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val urlAnimation by rememberRiveComposition {
            RiveCompositionSpec.url(
                url = "https://cdn.rive.app/animations/off_road_car_v7.riv"
            )
        }

        val resourceAnimation by rememberRiveComposition {
            RiveCompositionSpec.byteArray(
                byteArray = Res.readBytes("files/mode_switch.riv")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CustomRiveAnimation(
                modifier = Modifier.fillMaxWidth().weight(1f),
                composition = urlAnimation
            )
            CustomRiveAnimation(
                modifier = Modifier.fillMaxWidth().weight(1f),
                composition = resourceAnimation
            )
        }
    }
}