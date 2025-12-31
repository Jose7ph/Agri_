package com.jiagu.ags4.scene.device

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.ui.components.RadarBox
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString

fun conv_distance(d: Float): String {
    if (d == 0f) return ""
    return d.toString(2)
}

@Composable
fun Device360Radar() {
    val data = DroneModel.radarGraphData.observeAsState()
    val levels = data.value?.distances ?: FloatArray(8)
    Box(modifier = Modifier.fillMaxSize()) {
        RadarBox(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center),
            centerSize = (120 * 0.29).dp
        )
        Text(text = conv_distance(levels[0]), modifier = Modifier
            .align(Alignment.Center)
            .offset(0.dp, (-70).dp))
        Text(text = conv_distance(levels[1]), modifier = Modifier
            .align(Alignment.Center)
            .offset(75.dp, (-40).dp))
        Text(text = conv_distance(levels[2]), modifier = Modifier
            .align(Alignment.Center)
            .offset(85.dp, 0.dp))
        Text(text = conv_distance(levels[3]), modifier = Modifier
            .align(Alignment.Center)
            .offset(70.dp, 40.dp))
        Text(text = conv_distance(levels[4]), modifier = Modifier
            .align(Alignment.Center)
            .offset(0.dp, 70.dp))
        Text(text = conv_distance(levels[5]), modifier = Modifier
            .align(Alignment.Center)
            .offset((-70).dp, 40.dp))
        Text(text = conv_distance(levels[6]), modifier = Modifier
            .align(Alignment.Center)
            .offset((-85).dp, 0.dp))
        Text(text = conv_distance(levels[7]), modifier = Modifier
            .align(Alignment.Center)
            .offset((-75).dp, (-40).dp))
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun DeviceRadarPreview() {
    ComposeTheme {
        Device360Radar()
    }

}
