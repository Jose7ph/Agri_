package com.jiagu.jgcompose.popup

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagutech.jgcompose.ui.slider.Slider

/**
 * 警告弹窗
 * 警告弹窗需要通过滑块确认 无确定按钮
 * @param content 文本
 * @param title 标题 可选
 * @param titleColor 标题颜色 默认黑
 * @param contentColor 文本颜色 默认黑
 * @param cancelText 取消按钮文本
 * @param onConfirm 确定按钮事件
 * @param onDismiss 取消按钮事件
 */
@Composable
fun WarningPopup(
    content: String? = null,
    title: String? = null,
    titleColor: Color = Color.Black,
    contentColor: Color = Color.Black,
    cancelText: Int = R.string.cancel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        width = 300.dp, content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                content?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(sliderWidth = 260.dp,
                        sliderHeight = 40.dp,
                        sliderTitle = stringResource(id = R.string.slider_tip),
                        onSuccess = { success ->
                            if (success) {
                                onConfirm()
                            }
                        })
                }
            }
        }, showCancel = true, showConfirm = false, cancelText = cancelText, onDismiss = onDismiss
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun WarningPopupPreview() {
    ComposeTheme {
        Column {
            WarningPopup(title = "警告",
//                content = "",
                onConfirm = {},
                onDismiss = {})
        }
    }
}