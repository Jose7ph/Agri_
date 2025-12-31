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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * Group button
 *
 * @param modifier 装饰器
 * @param items 按钮文本列表 列表数量需要跟indexes一致
 * @param number 已选择的index
 * @param indexes item列表对应的index 列表数量需要跟items一致
 * @param onClick 点击回调 p1:点击的value,p2:点击的文本
 */
@Composable
fun GroupButton(
    modifier: Modifier = Modifier,
    number: Int,
    indexes: List<Int>,
    items: List<String>,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onClick: (Int, String) -> Unit,
) {
    val extendedColors = LocalExtendedColors.current
    var selected by remember {
        mutableIntStateOf(number)
    }
    LaunchedEffect(number) {
        selected = number
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
            val isSelected = selected == index
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
                        selected = index
                        onClick(index, item)
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

/**
 * Group ask button
 *
 * @param modifier 装饰器
 * @param items 按钮文本列表 列表数量需要跟indexes一致
 * @param number 已选择的index
 * @param indexes item列表对应的index 列表数量需要跟items一致
 * @param askPopup 提示弹窗 param1:点击的value,param2:点击的文本,param3:弹窗点击反馈
 */
@Composable
fun GroupAskButton(
    modifier: Modifier = Modifier,
    number: Int,
    indexes: List<Int>,
    items: List<String>,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    askPopup: @Composable (Int, String, (Boolean) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    var selected by remember {
        mutableIntStateOf(number)
    }
    var oldSelected by remember {
        mutableIntStateOf(number)
    }
    LaunchedEffect(number) {
        selected = number
        oldSelected = number
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
            val isSelected = selected == index
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
                        context.showDialog {
                            selected = index
                            askPopup(index, item) { success ->
                                if (!success) selected = oldSelected //false 值还原
                                context.hideDialog()
                            }
                        }

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
            GroupButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                items = listOf("1", "2", "3", "4", "5"),
                indexes = listOf(0, 1, 2, 3, 4),
                number = -1,
            ) { _, _ ->
            }
        }
    }
}