package com.jiagu.jgcompose.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * 基础弹窗 自定义弹窗用
 *
 * @param content 弹窗内容
 * @param width 弹窗最大宽度
 * @param onDismiss 取消回调
 * @param onConfirm 确定回调
 * @param showConfirm 是否显示确定
 * @param confirmText 确定按钮文本
 * @param showCancel 是否显示取消
 * @param cancelText 取消按钮文本
 * @param confirmEnable 确定是否可以点
 * @param cancelEnable 取消是否可以点
 */
@Composable
fun ScreenPopup(
    content: @Composable () -> Unit,
    width: Dp = 320.dp,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    showConfirm: Boolean = true,
    confirmText: Int = R.string.confirm,
    showCancel: Boolean = true,
    cancelText: Int = R.string.cancel,
    confirmEnable: Boolean = true,
    cancelEnable: Boolean = true
) {
    val extendedColors = LocalExtendedColors.current
    Box(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxSize()
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // 半透明黑色背景
            .noEffectClickable(enabled = !showCancel) {
                onDismiss()
            } // 允许点击穿透到下面的clickable修饰符
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.width(width)) {
                content() // 传入的内容视图
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) { // 底部按钮
                    val cancelColor =
                        if (cancelEnable) extendedColors.cancel else extendedColors.buttonDisabled
                    if (showCancel) Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .weight(1f)
                        .background(cancelColor)
                        .clickable(cancelEnable) {
                            onDismiss()
                        }) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(id = cancelText),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    val confirmColor =
                        if (confirmEnable) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled
                    if (showConfirm) Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .weight(1f)
                        .background(confirmColor)
                        .clickable(confirmEnable) {
                            onConfirm()
                        }) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(id = confirmText),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ScreenPopupPreview() {
    ComposeTheme {
        Column {
            ScreenPopup(content = {
                Box(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "这是一个弹窗",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

            }, onDismiss = {}, onConfirm = {})
        }
    }
}