package com.jiagu.jgcompose.radar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme
import kotlin.math.min


@Composable
fun Radar(
    sectorDistances: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    backgroundColor: Color = Color.Black,
) {
    //每层占比
    val radiusFactors = listOf(0.39f, 0.56f, 0.73f, 0.9f)
    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center){
        //黑色圆背景
        Circle(
            modifier = Modifier.fillMaxSize(), color = backgroundColor
        )
        //多层圆环
        MultiLayerCircular(
            modifier = Modifier.fillMaxSize(),
            sectorDistances = sectorDistances,
            colors = listOf(Color.Green, Color.Yellow, Color.Red, Color.Red),
            radiusFactors = radiusFactors,
        )
        //中心圆
        Circle(
            modifier = Modifier.fillMaxSize(0.28f), color = Color.LightGray
        )
    }
}

/**
 * 多层圆环
 */
@Composable
fun MultiLayerCircular(
    modifier: Modifier = Modifier,
    sectorDistances: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    strokeWidth: Dp = 6.dp,
    colors: List<Color> = listOf(Color.Green, Color.Yellow, Color.Red, Color.Red),
    radiusFactors: List<Float> = listOf(0.39f, 0.56f, 0.73f, 0.9f), // 不同层的半径因子
) {
    Canvas(
        modifier = modifier
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 计算圆心坐标
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        // 计算最小边界
        val minCanvasSize = min(canvasWidth, canvasHeight)
        // 绘制每层圆环
        for (radiusFactor in radiusFactors) {
            val radius = minCanvasSize * radiusFactor / 2
            val arcBounds = Rect(
                centerX - radius, centerY - radius, centerX + radius, centerY + radius
            )

            // 绘制扇形
            drawArc(
                color = Color.Gray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx()),
                topLeft = Offset(arcBounds.left, arcBounds.top),
                size = Size(arcBounds.width, arcBounds.height)
            )
        }

        val sectorAngle = 360f / sectorDistances.size
        var startAngle = -90 - sectorAngle / 2
        for (lv in sectorDistances) {
            if (lv > 0) {
                val deep = when {
                    lv >= 20 -> 1
                    lv >= 10 -> 2
                    lv >= 5 -> 3
                    else -> 4
                }
                for (idx in 0 until deep) {
                    val r = minCanvasSize * radiusFactors[3 - idx] / 2
                    val arcBounds = Rect(centerX - r, centerY - r, centerX + r, centerY + r)
                    drawArc(
                        color = colors[deep - 1],
                        startAngle = startAngle,
                        sweepAngle = sectorAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx()),
                        topLeft = Offset(arcBounds.left, arcBounds.top),
                        size = Size(arcBounds.width, arcBounds.height)
                    )
                }
            }
            // 更新起始角度
            startAngle += sectorAngle
        }
    }
}


@Composable
fun Circle(modifier: Modifier = Modifier, color: Color) {
    Canvas(
        modifier = modifier
    ) {
        drawCircle(
            color = color, // 圆形的颜色
            center = Offset(size.width / 2, size.height / 2), // 圆形的中心点
            radius = size.minDimension / 2, // 圆形的半径
        )
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RadarPreview() {
    ComposeTheme {
        Column {
            Radar(sectorDistances = floatArrayOf(0.3f))
        }
    }
}