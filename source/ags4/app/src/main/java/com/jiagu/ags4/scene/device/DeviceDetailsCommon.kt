package com.jiagu.ags4.scene.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.vm.task.UpgradeAppTask
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * 实时数据卡片
 */
@Composable
fun ParameterDataCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String = EMPTY_TEXT,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = Color.Black
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            )
            .clip(MaterialTheme.shapes.small), verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary
                ), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = style,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = content,
                modifier = Modifier.fillMaxWidth(),
                style = style,
                color = textColor
            )
        }
    }
}

/**
 * 设备详情通用button
 */
@Composable
fun DeviceDetailsCommonButton(
    text: String,
    enable: Boolean = true,
    buttonWidth: Dp = 180.dp,
    buttonHeight: Dp = 40.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight),
        enabled = enable,
        contentPadding = PaddingValues(0.dp),
    ) {
        AutoScrollingText(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * warn button
 */
@Composable
fun WarnButton(color: Color, onClick: () -> Unit) {
    Icon(
        painter = painterResource(id = R.drawable.default_warning),
        contentDescription = "warning",
        tint = color,
        modifier = Modifier
            .size(24.dp)
            .clickable {
                onClick()
            }
    )
}

@Composable
fun CardUpgradeTextRow(title: String, text: String, upgrade: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AutoScrollingText(
            text = title,
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
        AutoScrollingText(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (upgrade) Color.Red else Color.Black,
            textAlign = TextAlign.End
        )
    }
}


/**
 * Card frame
 *
 * @param modifier 装饰器
 * @param title 卡片标题
 * @param firmwareType 固件类型 在线升级用
 * @param upgrade 是否需要升级
 * @param showUpgradeLog 是否显示更新日志
 * @param showUpgrade 是否显示升级按钮
 * @param content 自定义显示文本
 * @param afterContent 后置自定义显示文本
 */
@Composable
fun CardFrame(
    modifier: Modifier = Modifier,
    title: String,
    firmwareType: FirmwareTypeEnum? = null,
    manufacturer: String = "",
    sn: String = "",
    version: String = "",
    upgrade: Boolean = false,
    showUpgradeLog: Boolean = false,
    showOnlineUpgrade: Boolean = true,
    showLocalUpgrade: Boolean = true,
    customUpgrade: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
    afterContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val act = context as DeviceManagementActivity
    Column(
        modifier = modifier
            .border(
                width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.small
            )
            .shadow(elevation = 16.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = Color.White, shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                modifier = Modifier,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
            if (showUpgradeLog) {
                CardButton(text = stringResource(id = R.string.change_log)) {
                    if (AgsUser.appChangeLog.isNotEmpty()) {
                        context.showDialog {
                            ScreenPopup(width = 480.dp,
                                content = {
                                    Box(
                                        modifier = Modifier
                                            .height(260.dp)
                                            .fillMaxWidth()
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(20.dp)
                                        ) {
                                            item {
                                                MarkdownText(
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth(),
                                                    markdown = AgsUser.appChangeLog,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
                                    }
                                },
                                showCancel = false,
                                showConfirm = true,
                                onDismiss = { context.hideDialog() },
                                onConfirm = { context.hideDialog() })
                        }
                    }
                }
            }
        }
        content()
        //升级
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            customUpgrade()
            //本地升级
            if (showLocalUpgrade) {
                Box(
                    modifier = Modifier.width(100.dp), contentAlignment = Alignment.CenterStart
                ) {
                    if (firmwareType != null) {
                        CardButton(text = stringResource(id = R.string.local_upgrade)) {
                            act.upgrade(firmwareType, false, manufacturer, sn, version)
                        }
                    }
                }
            }
            if (showOnlineUpgrade) {
                //在线升级
                Box(
                    modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd
                ) {
                    CardButton(
                        text = stringResource(id = R.string.upgrade),
                        enable = upgrade
                    ) {
                        if (firmwareType == null) {
                            context.startProgress(UpgradeAppTask())
                        } else {
                            act.upgrade(firmwareType, true, manufacturer, sn, version)
                        }
                    }
                }
            }
        }
        afterContent?.let {
            it()
        }
    }
}

@Composable
fun CardButton(text: String, enable: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .widthIn(min = 100.dp, max = 120.dp)
            .height(30.dp),
        enabled = enable
    ) {
        AutoScrollingText(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = Color.White
        )
    }
}