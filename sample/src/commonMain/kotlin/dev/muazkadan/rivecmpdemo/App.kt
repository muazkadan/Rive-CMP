package dev.muazkadan.rivecmpdemo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.rememberRiveComposition
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import org.jetbrains.compose.ui.tooling.preview.Preview
import rivecmp.sample.generated.resources.Res

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF001C1C)
        ) {

            val urlAnimation by rememberRiveComposition {
                RiveCompositionSpec.url("https://cdn.rive.app/animations/vehicles.riv")
            }

            val urlAnimation2 by rememberRiveComposition {
                RiveCompositionSpec.url("https://cdn.rive.app/animations/off_road_car_v7.riv")
            }

            val resourceAnimation by rememberRiveComposition {
                RiveCompositionSpec.byteArray(Res.readBytes("files/mode_switch.riv"))
            }


            Column {
                CustomRiveAnimation(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    composition = urlAnimation
                )

                CustomRiveAnimation(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    composition = urlAnimation2,
                )

                CustomRiveAnimation(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    composition = resourceAnimation,
                )
            }
        }
    }
}