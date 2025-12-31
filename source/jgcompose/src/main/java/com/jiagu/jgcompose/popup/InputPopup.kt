package com.jiagu.jgcompose.popup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 输入弹窗
 * 输入框必须有值才能点击确定
 * @param title 标题 可选
 * @param titleColor 标题颜色 默认 黑
 * @param hint 提示文本
 * @param confirmText 确定按钮文本
 * @param cancelText 取消按钮文本
 * @param showConfirm 确定按钮显示 默认 true
 * @param showCancel 取消按钮显示 默认 true
 * @param isLengthLimit 长度限制开关
 * @param showLengthLimit 显示长度限制 默认跟isLengthLimit一致
 * @param maxInputLength 最大文本长度
 * @param textAlign 文本对齐
 * @param textValidator 文本校验方法
 * @param defaultText 默认文本
 * @param width 弹窗最大宽度
 * @param onConfirm 点击确定事件
 * @param onDismiss 点击取消事件
 */
@Composable
fun InputPopup(
    title: String? = null,
    titleColor: Color = Color.Black,
    defaultText: String = "",
    hint: String = "",
    width: Dp = 300.dp,
    textAlign: TextAlign = TextAlign.Center,
    confirmText: Int = R.string.confirm,
    cancelText: Int = R.string.cancel,
    showCancel: Boolean = true,
    showConfirm: Boolean = true,
    isLengthLimit: Boolean = false,
    showLengthLimit: Boolean = isLengthLimit,
    maxInputLength: Int = 10,
    textValidator: ((String) -> Boolean)? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember {
        mutableStateOf(defaultText)
    }
    val validator = if (textValidator != null && text.isNotEmpty()) {
        textValidator(text)
    } else {
        true
    }
    ScreenPopup(
        width = width,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                NormalTextField(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .height(30.dp)
                        .fillMaxWidth(0.7f),
                    text = text,
                    textStyle = TextStyle.Default.copy(
                        textAlign = textAlign, color = Color.Black
                    ),
                    hint = hint,
                    hintPosition = textAlign,
                    onValueChange = {
                        text = it
                    },
                    borderColor = if (!validator) Color.Red else MaterialTheme.colorScheme.outline,
                    showClearIcon = false,
                    isLengthLimit = isLengthLimit,
                    showLengthLimit = showLengthLimit,
                    maxInputLength = maxInputLength,
                )
            }
        },
        onConfirm = {
            onConfirm(text)
        },
        onDismiss = onDismiss,
        showConfirm = showConfirm,
        showCancel = showCancel,
        confirmText = confirmText,
        cancelText = cancelText,
        confirmEnable = text.isNotEmpty() && validator
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun InputPopupPreview() {
    ComposeTheme {
        Column {
            InputPopup(title = "标题", hint = "请输入", onConfirm = {}, onDismiss = {})
        }
    }
}