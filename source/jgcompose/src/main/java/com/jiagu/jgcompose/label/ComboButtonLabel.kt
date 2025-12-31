package com.jiagu.jgcompose.label

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.button.ComboListButton
import com.jiagu.jgcompose.button.ComboRollButton
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 滚动选择器标签
 *
 * @param modifier 装饰器
 * @param labelName 标签名
 * @param labelWidth 标签宽度
 * @param comboIndex 当前已选择的索引
 * @param comboValue 当前显示值
 * @param comboItems 可选择的值
 * @param hint 提示文本
 * @param onConfirm 确定回调
 * @param onCancel 取消回调
 */
@Composable
fun ComboRollButtonLabel(
    modifier: Modifier = Modifier,
    labelName: String,
    labelWidth: Dp,
    comboIndex: Int,
    comboValue: String,
    comboItems: List<String>,
    hint: String = "",
    onConfirm: (Int, String) -> Unit,
    onCancel: () -> Unit
) {
    Label(modifier = modifier, labelName = labelName, labelWidth = labelWidth, content = {
        ComboRollButton(
            modifier = Modifier.fillMaxSize(),
            index = comboIndex,
            value = comboValue,
            items = comboItems,
            iconColor = Color.Black,
            backgroundColor = Color.Transparent,
            textColor = Color.Black,
            textStyle = MaterialTheme.typography.bodyMedium,
            onCancel = onCancel,
            onConfirm = onConfirm
        )
        if (comboValue.isEmpty()) {
            AutoScrollingText(
                text = hint,
                color = Color.Gray,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(end = 20.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    })
}

/**
 * 列表选择器标签
 *
 * @param modifier 装饰器
 * @param labelName 标签名
 * @param labelWidth 标签宽度
 * @param comboIndex 当前已选择的索引
 * @param comboValue 当前显示值
 * @param comboItems 可选择的值
 * @param hint 提示文本
 * @param onConfirm 确定回调
 * @param onCancel 取消回调
 */
@Composable
fun ComboListButtonLabel(
    modifier: Modifier = Modifier,
    labelName: String,
    labelWidth: Dp,
    comboIndex: Int,
    comboValue: String,
    comboItems: List<String>,
    hint: String = "",
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Label(modifier = modifier, labelName = labelName, labelWidth = labelWidth, content = {
        ComboListButton(
            modifier = Modifier.fillMaxSize(),
            index = comboIndex,
            value = comboValue,
            items = comboItems,
            iconColor = Color.Black,
            backgroundColor = Color.Transparent,
            textColor = Color.Black,
            onCancel = onCancel,
            onConfirm = {
                onConfirm(it)
            }
        )
        if (comboValue.isEmpty()) {
            AutoScrollingText(
                text = hint,
                color = Color.Gray,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(end = 20.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    })
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ComboButtonLabelPreview() {
    ComposeTheme {
        Column {
            ComboRollButtonLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelName = "选择",
                labelWidth = 120.dp,
                comboIndex = 0,
                comboItems = listOf("bbb", "aaa", "ccc"),
                comboValue = "111111111",
                hint = "点击选择",
                onConfirm = { _, _ -> },
                onCancel = {}
            )
        }
    }
}