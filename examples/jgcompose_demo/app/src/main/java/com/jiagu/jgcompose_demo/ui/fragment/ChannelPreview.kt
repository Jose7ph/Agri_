package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.channel.Channel
import com.jiagu.jgcompose.channel.ChannelCommand
import com.jiagu.jgcompose.channel.ChannelInfo
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllChannelsPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Channel(
                modifier = Modifier.height(40.dp),
                channelInfo = ChannelInfo(
                    keysName = "Channel 1",
                    commandTitles = listOf("Low", "Mid", "High"),
                    mappingNames = listOf("Mapping A", "Mapping B"),
                    onConfirm = { _, _ -> },
                    onCancel = {},
                )
            )
            ChannelCommand(commandTitles = listOf("A", "B", "C"))
        }
    }
}
