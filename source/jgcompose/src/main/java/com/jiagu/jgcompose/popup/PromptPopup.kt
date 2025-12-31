package com.jiagu.jgcompose.popup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 提示弹窗
 * 提示用，标题可选
 * @param content 文本
 * @param title 标题 可选
 * @param titleColor 标题颜色 默认 黑
 * @param contentColor 文本颜色 默认 黑
 * @param confirmText 确定按钮文本
 * @param cancelText 取消按钮文本
 * @param showConfirm 确定按钮显示 默认 true
 * @param showCancel 取消按钮显示 默认 true
 * @param onConfirm 点击确定事件
 * @param onDismiss 点击取消事件
 */
@Composable
fun PromptPopup(
    content: String,
    title: String? = null,
    titleColor: Color = Color.Black,
    contentColor: Color = Color.Black,
    confirmText: Int = R.string.confirm,
    cancelText: Int = R.string.cancel,
    showCancel: Boolean = true,
    showConfirm: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        width = 300.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
            ) {
                title?.let {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp, vertical = if (title == null) 20.dp else 10.dp
                        ), contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }
            }
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        showConfirm = showConfirm,
        showCancel = showCancel,
        confirmText = confirmText,
        cancelText = cancelText
    )
}

/**
 * 提示弹窗
 * 提示用，标题可选
 * @param content 构建的字符串 可自定义样式
 * @param title 标题 可选
 * @param titleColor 标题颜色 默认 黑
 * @param confirmText 确定按钮文本
 * @param cancelText 取消按钮文本
 * @param showConfirm 确定按钮显示 默认 true
 * @param showCancel 取消按钮显示 默认 true
 * @param onConfirm 点击确定事件
 * @param onDismiss 点击取消事件
 */
@Composable
fun PromptPopup(
    content: AnnotatedString,
    title: String? = null,
    titleColor: Color = Color.Black,
    confirmText: Int = R.string.confirm,
    cancelText: Int = R.string.cancel,
    showCancel: Boolean = true,
    showConfirm: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        width = 300.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
            ) {
                title?.let {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp, vertical = if (title == null) 20.dp else 10.dp
                        ), contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        showConfirm = showConfirm,
        showCancel = showCancel,
        confirmText = confirmText,
        cancelText = cancelText
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun PromptPopupPreview() {
    ComposeTheme {
        Column {
            PromptPopup(title = "提示",
                content = "这是一个提示框",
                onDismiss = {},
                onConfirm = {})
        }
    }
}