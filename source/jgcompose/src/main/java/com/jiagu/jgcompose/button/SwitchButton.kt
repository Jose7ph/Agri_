package com.jiagu.jgcompose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * 开关切换按钮
 *
 * @param width 按钮宽度
 * @param height 按钮高度
 * @param defaultChecked 开关默认状态
 * @param clickEnable 是否可以点击
 * @param switchColors 按钮切换颜色
 * @param backgroundColors 按钮背景色
 * @param onCheckedChange 切换回调
 */
@Composable
fun SwitchButton(
    width: Dp = 60.dp,
    height: Dp = 30.dp,
    defaultChecked: Boolean,
    clickEnable: Boolean = true,
    switchColors: List<Color> = listOf(Color.Gray, MaterialTheme.colorScheme.onPrimary),
    backgroundColors: List<Color> = listOf(
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primary
    ),
    onCheckedChange: (Boolean) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    var checked by remember { mutableStateOf(defaultChecked) }
    LaunchedEffect(defaultChecked) {
        if (defaultChecked != checked)
            checked = defaultChecked
    }
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .then(
                if (!clickEnable) {
                    Modifier
                        .background(
                            color = extendedColors.buttonDisabled,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                } else {
                    Modifier
                        .background(
                            color = if (checked) backgroundColors[1] else backgroundColors[0],
                            shape = MaterialTheme.shapes.extraLarge
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // 取消点击效果
                        ) {
                            checked = !checked
                            onCheckedChange(checked)
                        }
                }
            )
            .clip(shape = MaterialTheme.shapes.extraLarge),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        val circleSize = height * 0.6f
        val disabledCircleColor = if (checked) MaterialTheme.colorScheme.primary else Color.DarkGray
        val circleColor = if (checked) switchColors[1] else switchColors[0]
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(circleSize)
                .background(
                    color = if (clickEnable) circleColor else disabledCircleColor,
                    shape = CircleShape
                )
        )
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200)
@Composable
private fun SwitchPreview() {
    ComposeTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SwitchButton(
                width = 60.dp,
                height = 30.dp,
                defaultChecked = false,
                clickEnable = true
            ) {

            }
        }
    }

}