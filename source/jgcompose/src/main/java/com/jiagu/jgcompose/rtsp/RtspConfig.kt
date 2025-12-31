package com.jiagu.jgcompose.rtsp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.button.ComboRollButton
import com.jiagu.jgcompose.button.RadioButton
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.theme.ComposeTheme

class RtspInfo(val key: String, val url: String, var type: Int)

/**
 * Rtsp config
 *
 * @param modifier 装饰器
 * @param titleColor 标题颜色
 * @param titleStyle 标题样式
 * @param rtspInfoList rtsp列表
 * @param channels 通道列表
 * @param upDownChannel 上下按钮对应通道值
 * @param leftRightChannel 左右按钮对应通道值
 * @param key 当前选择的rtsp
 * @param customUrl 自定义url值
 * @param onRowClick 点击一行回调
 * @param onCancel 取消回调
 * @param onConfirm 按键绑定通道后的回调 p1：上下按钮对应通道 p2：左右按钮对应通道
 */
@Composable
fun RtspConfig(
    modifier: Modifier = Modifier,
    titleColor: Color = Color.Black,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    rtspInfoList: List<RtspInfo>,
    channels: List<String> = listOf(),
    upDownChannel: String = "N/A",
    leftRightChannel: String = "N/A",
    key: String = "",
    customUrl: String = "",
    onRowClick: (String, String) -> Unit,
    toast: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    onConfirm: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var selectedKey by remember {
        mutableStateOf(key)
    }
    var upDown by remember {
        mutableStateOf(upDownChannel)
    }
    var leftRight by remember {
        mutableStateOf(leftRightChannel)
    }

    Column(
        modifier = modifier
            .border(
                width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium
            )
            .background(Color.White, MaterialTheme.shapes.medium)
            .padding(vertical = 10.dp, horizontal = 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = stringResource(id = R.string.set_rtsp), color = titleColor, style = titleStyle)
        repeat(rtspInfoList.size) {
            val rtspInfo = rtspInfoList[it]
            val isSelected = selectedKey == rtspInfo.key
            RtspRow(rtspInfo = rtspInfo, isSelected = isSelected, onClick = {
                if (isSelected) {
                    selectedKey = ""
                    onRowClick("", "")
                } else {
                    selectedKey = rtspInfo.key
                    onRowClick(rtspInfo.key, rtspInfo.url)
                }
            })
            if (isSelected) {
                when (rtspInfo.type) {
                    1 -> {
                        ControlButton(
                            modifier = Modifier.padding(start = 60.dp),
                            title = stringResource(id = R.string.up_down) + ":",
                            text = upDown,
                            channels = channels,
                            onConfirm = { channel ->
                                upDown = channel
                                onConfirm(upDown, leftRight)
                            },
                            onCancel = onCancel,
                        )
                    }

                    2 -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 60.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ControlButton(
                                modifier = Modifier,
                                title = stringResource(id = R.string.up_down) + ":",
                                text = upDown,
                                channels = channels,
                                onConfirm = { channel ->
                                    upDown = channel
                                    onConfirm(upDown, leftRight)
                                },
                                onCancel = onCancel,
                            )
                            ControlButton(
                                modifier = Modifier,
                                title = stringResource(id = R.string.left_right) + ":",
                                text = leftRight,
                                channels = channels,
                                onConfirm = { channel ->
                                    leftRight = channel
                                    onConfirm(upDown, leftRight)
                                },
                                onCancel = onCancel,
                            )
                        }
                    }
                }
            }
        }
        CustomRtspRow(url = customUrl, isSelected = selectedKey == "custom", onClick = { inputUrl ->
            if (selectedKey == "custom") {
                selectedKey = ""
                onRowClick("", "")
            } else {
                selectedKey = "custom"
                onRowClick("custom", inputUrl)
            }
        }, onButtonClick = { inputUrl ->
            if (selectedKey != "custom") {
                selectedKey = "custom"
            }
            onRowClick("custom", inputUrl)
            toast(context.getString(R.string.success))
        })
    }
}

@Composable
private fun RtspRow(
    rtspInfo: RtspInfo, isSelected: Boolean, onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable {
                    onClick()
                },
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //序号
            RadioButton(modifier = Modifier, isSelected = isSelected) {
                onClick()
            }
            // key
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = rtspInfo.key,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // url
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = rtspInfo.url,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 自定义rtsp输入
 *
 * @param url url地址
 * @param isSelected 是否选择
 * @param onClick 点击回调
 * @param onButtonClick 按钮点击回调
 */
@Composable
private fun CustomRtspRow(
    url: String = "",
    isSelected: Boolean,
    onClick: (String) -> Unit,
    onButtonClick: (String) -> Unit
) {
    var inputUrl by remember {
        mutableStateOf(url)
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable {
                    onClick(inputUrl)
                },
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //序号
            RadioButton(modifier = Modifier, isSelected = isSelected) {
                onClick(inputUrl)
            }
            Row(
                modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    NormalTextField(
                        text = inputUrl,
                        onValueChange = { value ->
                            inputUrl = value
                        },
                        hintPosition = TextAlign.Center,
                        modifier = Modifier.height(30.dp),
                        borderColor = Color.LightGray
                    )
                }
                Button(
                    modifier = Modifier
                        .width(80.dp)
                        .height(40.dp),
                    onClick = {
                        onButtonClick(inputUrl)
                    },
                    enabled = url != inputUrl,
                    contentPadding = PaddingValues(0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.setting),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

/**
 * 控制按钮
 *
 * @param title 标题
 * @param text 按钮名
 * @param channels 通道值
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 */
@Composable
private fun ControlButton(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    channels: List<String>,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = title, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        ComboRollButton(
            modifier = Modifier
                .width(80.dp)
                .height(30.dp),
            index = channels.indexOf(text),
            value = text,
            items = channels,
            backgroundColor = Color.Transparent,
            borderColor = Color.Gray,
            iconColor = Color.Black,
            textColor = Color.Black,
            onCancel = onCancel,
            onConfirm = { _, v ->
                onConfirm(v)
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 640)
@Composable
private fun RTSPConfigPreview() {
    ComposeTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            RtspConfig(
                rtspInfoList = listOf(
                    RtspInfo(key = "a", url = "aaaaaaa", type = 0),
                    RtspInfo(key = "b", url = "bbbbb", type = 1),
                    RtspInfo(key = "c", url = "ccccc", type = 1),
                    RtspInfo(key = "d", url = "dddd", type = 2),
                    RtspInfo(key = "e", url = "eeee", type = 2),
                ),
                onRowClick = { _, _ -> }
            )
        }
    }
}