package com.jiagu.jgcompose.remotecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.channel.Channel
import com.jiagu.jgcompose.channel.ChannelInfo
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 遥控器通道
 *
 * @param modifier 装饰器
 * @param channelInfos 通道信息
 */
@Composable
fun RemoteControlChannel(
    modifier: Modifier = Modifier, channelInfos: List<ChannelInfo>
) {
    val keysWith = 100.dp
    val mappingWidth = 120.dp
    val title = @Composable { text: String ->
        AutoScrollingText(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray
        )
    }
    Column(
        modifier = modifier
            .border(
                width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium
            )
            .background(Color.White, MaterialTheme.shapes.medium)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //title
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(keysWith)) {
                title(stringResource(id = R.string.keystroke))
            }
            Box(modifier = Modifier.weight(1f)) {
                title(stringResource(id = R.string.command))
            }
            Box(modifier = Modifier.width(mappingWidth)) {
                title(stringResource(id = R.string.mapping))
            }
        }
        //channels
        repeat(channelInfos.size) {
            val channelInfo = channelInfos[it]
            Channel(
                modifier = Modifier.height(40.dp),
                keysRowMaxWidth = keysWith,
                keyHeight = 30.dp,
                mappingRowMaxWidth = mappingWidth,
                mappingHeight = 30.dp,
                channelInfo = channelInfo
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RemoteControlChannelPreview() {
    ComposeTheme {
        Column {
            RemoteControlChannel(channelInfos = listOf())
        }
    }
}