package dev.muazkadan.rivecmpdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.RiveTrigger
import dev.muazkadan.rivecmp.rememberRiveComposition
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import rivecmp.sample.generated.resources.Res

/** Shows a data-bound graphic, counts the trigger firings it reports, and lets the user fire it. */
@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun DataBindingSample(modifier: Modifier = Modifier) {
    val composition by rememberRiveComposition(
        spec = { RiveCompositionSpec.byteArray(Res.readBytes("files/data_binding_test_triggers.riv")) }
    )
    var trigger by remember { mutableStateOf<RiveTrigger?>(null) }
    var firings by remember { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        trigger?.triggers?.collect { firings++ }
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CustomRiveAnimation(
            modifier = Modifier.size(96.dp),
            composition = composition,
            stateMachineName = "State Machine 1",
            onViewModelInstance = { trigger = it.trigger("trigger") },
        )
        Text(
            modifier = Modifier.weight(1f),
            text = if (trigger == null) "Not bound" else "Trigger reported $firings times",
            color = Color.White,
        )
        Button(onClick = { trigger?.trigger() }, enabled = trigger != null) {
            Text("Fire")
        }
    }
}
