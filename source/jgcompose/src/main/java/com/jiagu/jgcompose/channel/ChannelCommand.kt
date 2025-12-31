package com.jiagu.jgcompose.channel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 通道指令
 *
 * @param modifier 装饰器
 * @param commandNum 指令区域数量 默认3
 * @param commandTitles 指令区域对应文本 数量需要跟commandNum一致
 * @param commandProgress 指令进度 0f ~ 1f
 * @param titleColor 文本颜色
 * @param titleStyle 文本样式
 * @param tickWidth 角标宽度
 * @param tickHeight 角标高度
 */
@Composable
fun ChannelCommand(
    modifier: Modifier = Modifier,
    commandNum: Int = 3,
    commandTitles: List<String> = listOf(),
    commandProgress: Float = 0f,
    titleColor: Color = Color.Black,
    titleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    tickWidth: Dp = 10.dp,
    tickHeight: Dp = 15.dp
) {
    //角标位置初始化
    val switch = when {
        commandProgress < 0f -> -1
        commandProgress == 0f -> 0
        commandProgress > 0f && commandProgress <= 0.33f -> 1
        commandProgress > 0.33f && commandProgress <= 0.66f -> 2
        commandProgress > 0.66f && commandProgress < 1f -> 3
        commandProgress == 1f -> 4
        commandProgress > 1f -> 99
        else -> 0
    }
    val fillColorPadding = 20.dp
    //计算偏移量
    val offsetX = when {
        switch <= 3 -> -tickWidth / 2f
        else -> -tickWidth / 2f
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .padding(horizontal = fillColorPadding)
                    .background(color = Color.LightGray, shape = CircleShape),
            ) {
                //分割区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (i in 0..<commandNum) {
                        //进度
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f / commandNum)
                                .background(
                                    color = when (switch) {
                                        -1, 99 -> Color.Red
                                        else -> if (i < switch) MaterialTheme.colorScheme.primary else Color.Unspecified
                                    }, shape = CircleShape
                                )
                        ) {}
                        if (i != commandNum - 1) {
                            VerticalDivider(
                                thickness = 2.dp,
                                color = Color.White,
                                modifier = Modifier.fillMaxHeight()
                            )
                        }
                    }

                }
            }
            //三角形
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = fillColorPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier.fillMaxWidth(commandProgress),
                )
                TickBox(
                    offsetX = offsetX, width = tickWidth, height = tickHeight
                )
            }
        }
        if (commandTitles.isNotEmpty()) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = fillColorPadding, end = fillColorPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (title in commandTitles) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AutoScrollingText(
                            text = title,
                            modifier = Modifier.fillMaxWidth(),
                            color = titleColor,
                            style = titleStyle
                        )
                    }
                }
            }
        }
    }
}

/**
 * 标记图形
 *
 * @param offsetX x偏移量
 * @param width 标记宽度
 * @param height 标记高度
 */
@Composable
private fun TickBox(offsetX: Dp, width: Dp, height: Dp) {
    val tickWidthPx = with(LocalDensity.current) { width.toPx() }
    val tickHeightPx = with(LocalDensity.current) { height.toPx() }
    Box(
        modifier = Modifier.offset(x = offsetX), contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(width)
                .height(height),
        ) {
            val size = Size(width = tickWidthPx, height = tickHeightPx)
            val rect = Rect(Offset.Zero, size)
            val trianglePath = Path().apply {
                moveTo(rect.bottomLeft.x, rect.bottomLeft.y)
                lineTo(rect.topRight.x, rect.topRight.y)
                lineTo(rect.topLeft.x, rect.topLeft.y)
                lineTo(rect.bottomRight.x, rect.bottomRight.y)
            }
            drawIntoCanvas { canvas ->
                canvas.drawOutline(outline = Outline.Generic(trianglePath), paint = Paint().apply {
                    color = Color.Red
                })
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ChannelCommandPreview() {
    ComposeTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            ChannelCommand(commandTitles = listOf("A", "B", "C"))
        }
    }
}