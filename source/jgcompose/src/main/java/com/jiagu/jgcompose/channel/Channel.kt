package com.jiagu.jgcompose.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.button.ComboRollButton
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * 通道class
 *
 * @property keysName 按键名
 * @property commandProgress 指令值 0f ~ 1f
 * @property commandNum 指令数量
 * @property commandTitles 指令标题
 * @property mappingName 当前通道映射名称
 * @property mappingNames 通道映射列表
 * @property isMappingEdit 通道是否可以修改 默认可以
 * @property showDirection 是否显示方向 默认显示
 * @property isReverse 是否反向 默认正向
 * @property onCancel 取消回调
 * @property onConfirm 确定回调
 */
class ChannelInfo(
    val keysName: String,
    val commandProgress: Float = 0f,
    val commandNum: Int = 3,
    val commandTitles: List<String>,
    val mappingIndex: Int = 0,
    val mappingName: String = "N/A",
    val mappingNames: List<String>,
    val isMappingEdit: Boolean = true,
    val showDirection: Boolean = true,
    var mappingTips: List<String> = listOf(),
    val isReverse: Boolean = false,
    val onCancel: () -> Unit = {},
    val onConfirm: (Int, Boolean) -> Unit = { _, _ -> },
)

/**
 * 通道
 *
 * @param modifier 装饰器
 * @param channelInfo 通道对象
 * @param keysRowMaxWidth 按键行最大宽度
 * @param keyHeight 按键高度
 * @param mappingRowMaxWidth 映射行最大宽度
 * @param mappingHeight 映射高度
 */
@Composable
fun Channel(
    modifier: Modifier = Modifier,
    channelInfo: ChannelInfo,
    keysRowMaxWidth: Dp = 60.dp,
    keyHeight: Dp = Dp.Infinity,
    mappingRowMaxWidth: Dp = 120.dp,
    mappingHeight: Dp = Dp.Infinity,
) {
    val extendedColors = LocalExtendedColors.current
    val context = LocalContext.current
    val list = listOf(
        stringResource(R.string.positive),
        stringResource(R.string.reverse)
    )
    var reverse by remember { mutableStateOf(channelInfo.isReverse) }
    var mappingIndex by remember { mutableIntStateOf(channelInfo.mappingIndex) }
    LaunchedEffect(channelInfo.isReverse) {
        reverse = channelInfo.isReverse
    }
    LaunchedEffect(channelInfo.mappingIndex) {
        mappingIndex = channelInfo.mappingIndex
    }
    Row(
        modifier = modifier
            .then(
                if (channelInfo.showDirection) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                context.showDialog {
                                    ListSelectionPopup(
                                        title = stringResource(R.string.rocker_direction),
                                        list = list,
                                        defaultIndexes = listOf(if (reverse) 1 else 0),
                                        item = {
                                            Text(
                                                text = it,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        onConfirm = { indexes, _ ->
                                            if (indexes.isNotEmpty()) {
                                                //idx = 0 正向 idx = 1 反向
                                                reverse = indexes[0] == 1
                                                channelInfo.onConfirm(mappingIndex, reverse)
                                            }
                                            context.hideDialog()
                                        },
                                        onDismiss = {
                                            context.hideDialog()
                                        }
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //keys
        Box(
            modifier = Modifier
                .width(keysRowMaxWidth)
                .height(keyHeight),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = MaterialTheme.shapes.extraSmall
                    ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channelInfo.keysName,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        //command
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            ChannelCommand(
                modifier = Modifier.fillMaxWidth(),
                commandNum = channelInfo.commandNum,
                commandTitles = channelInfo.commandTitles,
                commandProgress = channelInfo.commandProgress,
            )
        }
        //mapping
        Box(
            modifier = Modifier
                .width(mappingRowMaxWidth)
                .fillMaxHeight()
        ) {
            if (channelInfo.isMappingEdit) {
                ComboRollButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mappingHeight),
                    index = mappingIndex,
                    value = channelInfo.mappingName,
                    items = channelInfo.mappingNames,
                    itemTips = channelInfo.mappingTips,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Gray,
                    iconColor = Color.Black,
                    textColor = Color.Black,
                    onCancel = channelInfo.onCancel,
                    onConfirm = { idx, _ ->
                        mappingIndex = idx
                        channelInfo.onConfirm(mappingIndex, reverse)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mappingHeight)
                        .background(
                            color = extendedColors.buttonDisabled,
                            shape = MaterialTheme.shapes.small
                        ), contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = channelInfo.mappingName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ChannelPreview() {
    ComposeTheme {
        Column(Modifier.padding(10.dp)) {
            Channel(
                modifier = Modifier.height(40.dp),
                channelInfo = ChannelInfo(
                    keysName = "AAAA",
                    commandTitles = listOf("1", "2", "3"),
                    mappingNames = listOf(),
                    onConfirm = { _, _ ->

                    },
                    onCancel = {},
                )
            )
        }
    }
}