package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.radar.Radar
import com.jiagu.jgcompose.radar.RadarInstrument
import com.jiagu.jgcompose.radar.RadarPositionEnum
import com.jiagu.jgcompose.radar.RadarTextData
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllRadarPreviews() {
    ComposeTheme {
        Column {
            Radar(sectorDistances = floatArrayOf(0.3f, 0.5f, 0.7f, 0.9f, 0.2f, 0.4f, 0.6f, 0.8f))
            RadarInstrument(
                modifier = Modifier
                    .width(300.dp)
                    .height(150.dp),
                radarPosition = RadarPositionEnum.RIGHT,
                sectorDistances = floatArrayOf(0.3f, 0.5f, 0.7f, 0.9f, 0.2f, 0.4f, 0.6f, 0.8f),
                content = {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadarTextData(
                            modifier = Modifier.weight(1f),
                            title = "param1",
                            text = "123",
                        )
                        RadarTextData(
                            modifier = Modifier.weight(1f),
                            title = "param2",
                            text = "456",
                        )
                    }
                }
            )
        }
    }
}