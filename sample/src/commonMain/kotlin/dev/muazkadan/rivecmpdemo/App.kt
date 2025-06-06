package dev.muazkadan.rivecmpdemo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import org.jetbrains.compose.ui.tooling.preview.Preview
import rivecmp.sample.generated.resources.Res

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var resourceAnimation by remember { mutableStateOf<ByteArray?>(null) }
        LaunchedEffect(Unit){
            resourceAnimation = Res.readBytes("files/mode_switch.riv")
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CustomRiveAnimation(
                modifier = Modifier.fillMaxWidth().weight(1f),
                url = "https://cdn.rive.app/animations/off_road_car_v7.riv"
            )
            resourceAnimation?.let {
                CustomRiveAnimation(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    byteArray = it
                )
            }
        }
    }
}