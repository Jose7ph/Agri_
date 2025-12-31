package com.jiagu.jgcompose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 倒计时按钮
 *
 * @param modifier 装饰器
 * @param waitTime 等待时间 单位秒
 * @param title 按钮文字
 * @param waitTitle 倒计时时显示的文字
 * @param shape 形状
 * @param textStyle 文字样式
 * @param showWaitTime 是否显示倒计时文字
 * @param onClick 点击事件
 */
@Composable
fun CountdownButton(
    modifier: Modifier = Modifier,
    waitTime: Float = 1f,
    title: String,
    waitTitle: (Float) -> String = { "" },
    shape: Shape = MaterialTheme.shapes.small,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    showWaitTime: Boolean = false,
    onClick: ((Boolean) -> Unit) -> Unit,
) {
    // 控制按钮是否可用
    var isEnabled by remember { mutableStateOf(true) }
    // 倒计时时间，使用 Float 类型
    var countdown by remember { mutableFloatStateOf(waitTime) }
    val extendedColor = LocalExtendedColors.current
    Box(
        modifier = modifier
            .clickable(
                enabled = isEnabled, onClick = {
                    onClick {
                        if (it) {
                            isEnabled = false
                            countdown = waitTime // 设置倒计时
                        }
                    }
                })
            .background(
                color = if (isEnabled) MaterialTheme.colorScheme.primary else extendedColor.buttonDisabled,
                shape = shape
            ), contentAlignment = Alignment.Center
    ) {
        if (!showWaitTime) {
            AutoScrollingText(
                text = title,
                color = Color.White,
                style = textStyle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            if (!isEnabled) {
                // 显示倒计时时间
                AutoScrollingText(
                    text = waitTitle(countdown),
                    color = Color.White,
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                AutoScrollingText(
                    text = title,
                    color = Color.White,
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // 倒计时逻辑
    LaunchedEffect(key1 = isEnabled, key2 = countdown) {
        if (!isEnabled && countdown > 0) {
            // 等待 100 毫秒，以便更精确地处理小数倒计时
            delay(100)
            countdown -= 0.1f
            if (countdown <= 0) {
                isEnabled = true
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun CountdownButtonPreview() {
    ComposeTheme {
        Column {
            CountdownButton(
                modifier = Modifier.size(200.dp, 60.dp),
                title = "click me",
                waitTime = 0.5f,
                showWaitTime = true,
                waitTitle = { "${it.roundToInt()}" },
                onClick = {
                    // 点击事件
                })
        }
    }
}