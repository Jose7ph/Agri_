package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.vm.DroneModel
import kotlin.math.min

/**
 * 雷达
 */
@Composable
fun RadarBox(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    color: Color = Color.White,
    centerSize: Dp = 0.dp
) {
    //每层占比
    val radiusFactors = listOf(0.39f, 0.56f, 0.73f, 0.9f)
    Surface(
        modifier = modifier
            .fillMaxSize()
            .offset(x = offsetX, y = offsetY),
        color = color,
        shape = CircleShape
    ) {
        //黑色圆背景
        Circle(
            modifier = Modifier.fillMaxSize(), color = Color.Black
        )
        //多层圆环
        MultiLayerCircular(
            modifier = Modifier.fillMaxSize(),
            colors = listOf(Color.Green, Color.Yellow, Color.Red, Color.Red),
            radiusFactors = radiusFactors,
        )
        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
            //中心圆
            Circle(
                modifier = Modifier.size(centerSize), color = Color.LightGray
            )
        }
    }
}

/**
 * 多层圆环
 */
@Composable
fun MultiLayerCircular(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 6.dp,
    colors: List<Color> = listOf(Color.Green, Color.Yellow, Color.Red, Color.Red),
    radiusFactors: List<Float> = listOf(0.39f, 0.56f, 0.73f, 0.9f), // 不同层的半径因子
) {
    var level = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    val graph = DroneModel.radarGraphData.observeAsState()
    if (graph.value != null) {
        level = graph.value!!.distances
    }

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

        val sectorAngle = 360f / level.size
        var startAngle = -90 - sectorAngle / 2
        for (lv in level) {
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

@Preview(showBackground = true)
@Composable
fun RadarBoxPreview(modifier: Modifier = Modifier) {
    RadarBox(
        modifier = Modifier.size(92.dp),
        centerSize = (90 * 0.29).dp
    )
}