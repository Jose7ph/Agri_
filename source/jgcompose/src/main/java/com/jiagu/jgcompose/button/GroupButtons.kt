package com.jiagu.jgcompose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * 多选Group buttons
 *
 * @param modifier 装饰器
 * @param items 按钮文本列表 列表数量需要跟indexes一致
 * @param numbers 已选择的index
 * @param indexes item列表对应的index 列表数量需要跟items一致
 * @param onClick 点击回调 p1:点击的values
 */
@Composable
fun GroupButtons(
    modifier: Modifier = Modifier,
    numbers: List<Int>,
    indexes: List<Int>,
    items: List<String>,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onClick: (List<Int>) -> Unit,
) {
    val extendedColors = LocalExtendedColors.current
    val selected = remember {
        mutableStateListOf(0)
    }
    LaunchedEffect(numbers) {
        selected.clear()
        selected.addAll(numbers)
    }
    Row(
        modifier = modifier.background(
            color = extendedColors.groupButton,
            shape = MaterialTheme.shapes.small
        )
    ) {
        repeat(items.size) {
            val item = items[it]
            val index = indexes[it]
            val isSelected = numbers.any { select -> select == index }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                color = if (enabled) MaterialTheme.colorScheme.primary else LocalExtendedColors.current.groupButtonDisabled,
                                shape = when (it) {
                                    0 -> RoundedCornerShape(
                                        topStart = 8.dp,
                                        bottomStart = 8.dp
                                    )

                                    items.lastIndex -> RoundedCornerShape(
                                        topEnd = 8.dp,
                                        bottomEnd = 8.dp,
                                    )

                                    else -> RectangleShape
                                }
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(enabled = enabled) {
                        if(isSelected) selected.remove(index) else selected.add(index)
                        onClick(selected)
                    },
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = item,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) Color.White else Color.Black,
                    style = textStyle
                )
            }
            if (it < items.lastIndex) {
                VerticalDivider(
                    thickness = 1.dp, color = Color.White, modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun GroupButtonPreview() {
    ComposeTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            GroupButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                items = listOf("1", "2", "3", "4", "5"),
                indexes = listOf(0, 1, 2, 3, 4),
                numbers = listOf(0, 1),
            ) {
            }
        }
    }
}