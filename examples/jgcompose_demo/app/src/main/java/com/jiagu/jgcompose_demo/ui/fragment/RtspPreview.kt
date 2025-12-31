package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.rtsp.RtspConfig
import com.jiagu.jgcompose.rtsp.RtspInfo
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllRtspPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RTSP configuration preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                RtspConfig(
                    rtspInfoList = listOf(
                        RtspInfo(key = "RTSP1", url = "rtsp://192.168.1.100:554/stream1", type = 0),
                        RtspInfo(key = "RTSP2", url = "rtsp://192.168.1.101:554/stream2", type = 1),
                        RtspInfo(key = "RTSP3", url = "rtsp://192.168.1.102:554/stream3", type = 2),
                    ),
                    channels = listOf("CH1", "CH2", "CH3", "CH4", "CH5", "CH6"),
                    upDownChannel = "CH1",
                    leftRightChannel = "CH2",
                    onRowClick = { _, _ -> },
                    onCancel = {},
                    onConfirm = { _, _ -> }
                )
            }
        }
    }
}