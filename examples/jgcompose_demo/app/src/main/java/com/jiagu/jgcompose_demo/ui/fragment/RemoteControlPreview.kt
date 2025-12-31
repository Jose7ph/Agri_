package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.channel.ChannelInfo
import com.jiagu.jgcompose.remotecontrol.RemoteControl
import com.jiagu.jgcompose.remotecontrol.RemoteControlChannel
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllRemoteControlPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Remote control main interface preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                RemoteControl(
                    modifier = Modifier.fillMaxSize(),
                    rockerType = 0,
                    rockerValues = floatArrayOf(0.5f, -0.3f, 0.0f, 0.7f),
                    onRockerMode = {},
                    onCalibration = {}
                )
            }
            
            // Remote control channel preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                RemoteControlChannel(
                    channelInfos = listOf(
                        ChannelInfo(
                            keysName = "CH1",
                            commandTitles = listOf("Roll", "Pitch", "Throttle"),
                            mappingName = "AUX1",
                            mappingNames = listOf("AUX1", "AUX2", "AUX3", "AUX4")
                        ),
                        ChannelInfo(
                            keysName = "CH2",
                            commandTitles = listOf("Roll", "Pitch", "Throttle"),
                            mappingName = "AUX2",
                            mappingNames = listOf("AUX1", "AUX2", "AUX3", "AUX4")
                        ),
                        ChannelInfo(
                            keysName = "CH3",
                            commandTitles = listOf("Roll", "Pitch", "Throttle"),
                            mappingName = "AUX3",
                            mappingNames = listOf("AUX1", "AUX2", "AUX3", "AUX4")
                        ),
                        ChannelInfo(
                            keysName = "CH4",
                            commandTitles = listOf("Roll", "Pitch", "Throttle"),
                            mappingName = "AUX4",
                            mappingNames = listOf("AUX1", "AUX2", "AUX3", "AUX4")
                        )
                    )
                )
            }
        }
    }
}