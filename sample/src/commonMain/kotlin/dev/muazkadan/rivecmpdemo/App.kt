package dev.muazkadan.rivecmpdemo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.muazkadan.rivecmp.CustomRiveAnimation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CustomRiveAnimation(
                modifier = Modifier.fillMaxWidth().weight(1f),
                url = "https://cdn.rive.app/animations/off_road_car_v7.riv"
            )
        }
    }
}