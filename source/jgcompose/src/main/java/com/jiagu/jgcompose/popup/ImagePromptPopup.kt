package com.jiagu.jgcompose.popup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.theme.ComposeTheme


/**
 * 图片弹窗
 *
 * @param image 图片id
 * @param imageSize 图片大小
 * @param content 弹窗内容 可选
 * @param title 弹窗标题 可选
 * @param titleColor 标题颜色 默认黑
 * @param contentColor 文本颜色 默认黑
 * @param showConfirm 确定按钮显示 默认 true
 * @param showCancel 取消按钮显示 默认 true
 * @param onConfirm 确定按钮事件
 * @param onDismiss 取消按钮事件
 */
@Composable
fun ImagePromptPopup(
    image: Int,
    imageSize: Dp,
    content: String? = null,
    title: String? = null,
    titleColor: Color = Color.Black,
    contentColor: Color = Color.Black,
    showCancel: Boolean = true,
    showConfirm: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "image",
                    modifier = Modifier.size(imageSize),
                )
                content?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp, vertical = 10.dp
                            ), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor
                        )
                    }
                }
            }
        },
        showConfirm = showConfirm,
        showCancel = showCancel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/**
 * 图片弹窗
 *
 * @param image 图片id
 * @param imageSize 图片大小
 * @param content 构建的字符串结构 可选
 * @param title 弹窗标题 可选
 * @param titleColor 标题颜色 默认黑
 * @param showConfirm 确定按钮显示 默认 true
 * @param showCancel 取消按钮显示 默认 true
 * @param onConfirm 确定按钮事件
 * @param onDismiss 取消按钮事件
 */
@Composable
fun ImagePromptPopup(
    image: Int,
    imageSize: Dp,
    content: AnnotatedString? = null,
    title: String? = null,
    titleColor: Color = Color.Black,
    showCancel: Boolean = true,
    showConfirm: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "image",
                    modifier = Modifier.size(imageSize),
                )
                content?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        showConfirm = showConfirm,
        showCancel = showCancel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ImagePromptPopupPreview() {
    ComposeTheme {
        Column {
            ImagePromptPopup(
                image = R.drawable.eye_open,
                imageSize = 180.dp,
                content = "这是一个图片",
                title = "图片",
                onConfirm = {},
                onDismiss = {})
        }
    }
}