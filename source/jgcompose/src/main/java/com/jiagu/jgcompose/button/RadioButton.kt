package com.jiagu.jgcompose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 单选按钮
 *
 * @param modifier 装饰器
 * @param isSelected 是否选择
 * @param size 按钮大小
 * @param onClick 点击回调 返回结果
 */
@Composable
fun RadioButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    size: Dp = 30.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color.LightGray,
                shape = CircleShape
            )
            .clip(shape = CircleShape)
            .clickable {
                onClick()
            }, contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "selected param",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RadioButton() {
    ComposeTheme {
        Column {
            RadioButton(isSelected = true) {}
            RadioButton(isSelected = false) {}
        }
    }
}