package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.ui.theme.BlackAlpha
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * canvasSize 圆环大小
 * centerCircleRadius 中心圆点半径
 * centerCircleColor 中心圆点颜色
 * hideWaitTime 隐藏等待时间s
 */
@Composable
fun RockerBox(
    modifier: Modifier = Modifier,
    canvasSize: Dp = 100.dp,
    centerCircleRadius: Float = 10f,
    centerCircleColor: Color = MaterialTheme.colorScheme.primary,
    hideWaitTime: Int = 5,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    onPositionChange: (Offset, Float) -> Unit,
) {

    var centerX = 0f
    var centerY = 0f
    var position by remember {
        mutableStateOf(
            Offset(centerX, centerY)
        )
    }
    var showRocker by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = position, key2 = showRocker) {
        if (showRocker) {
            var hideTime = hideWaitTime
            while (true) {
                hideTime--
                if (hideTime == 0) {
                    showRocker = false
                    break
                }
                //等待1s
                delay(1000)
            }
        }
    }

    //显示圆心按钮
    if (!showRocker) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd
        ) {
            Canvas(
                modifier = modifier
                    .padding(20.dp)
                    .clickable {
                        showRocker = true
                    }) {
                drawCircle(
                    color = centerCircleColor, // 圆形的颜色
                    center = Offset(centerX, centerY), // 圆形的中心点
                    radius = centerCircleRadius
                )
            }
        }
    }
    //显示摇杆
    if (showRocker) {
        Canvas(
            modifier = modifier
                .size(canvasSize)
        ) {
            drawCircle(
                color = BlackAlpha,
            )
            drawArc(
                color = lineColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx()),
            )
        }
        Canvas(
            modifier = modifier
                .size(canvasSize)
                .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            val newRawPosition = position + dragAmount
                            val newPosition = if (isInsideArc(
                                    newRawPosition,
                                    centerX,
                                    centerY,
                                    centerCircleRadius.coerceAtLeast(1f)
                                )
                            ) {
                                newRawPosition
                            } else {
                                // 如果在圆外，则将位置限制在圆的边界上
                                limitToCircle(newRawPosition, centerX)
                            }
                            position = newPosition
                            // 将 newPosition 转换为角度
                            val angle = calculateAngle(newPosition)
                            onPositionChange(newPosition, angle)
                            change.consume()
                        })
                }) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            // 计算圆心坐标
            centerX = canvasWidth / 2
            centerY = canvasHeight / 2
            drawLine(
                color = lineColor,
                start = Offset(centerX, centerY),
                end = Offset(centerX - position.x, centerY - position.y),
                strokeWidth = 8f
            )
            drawCircle(
                color = centerCircleColor, // 圆形的颜色
                center = Offset(centerX, centerY), // 圆形的中心点
                radius = centerCircleRadius
            )
        }
    }

}

// 如果位置在圆外，则将位置限制在圆的边界上
private fun limitToCircle(position: Offset, radius: Float): Offset {
    val angle = atan2(position.y, position.x)
    val x = cos(angle) * radius
    val y = sin(angle) * radius
    return Offset(x, y)
}

// 检查位置是否在圆内
private fun isInsideArc(point: Offset, radius: Float, centerX: Float, centerY: Float): Boolean {
    val angle = atan2(point.y - centerY, point.x - centerX)
    val distance = hypot(point.x - centerX, point.y - centerY)
    return distance in radius..radius && (angle in 0.0..360.0)

}

// 计算从圆心到 Offset 对象指定位置的角度
private fun calculateAngle(point: Offset): Float {
    // 转换角度时将改变Y轴方向，使上方为正方向
    val angleRad = atan2(-point.y, point.x)
    var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
//    if (angleDeg < 0) {
//        angleDeg += 360f // 将负角度转换为正角度
//    } else if (angleDeg >= 360f) {
//        angleDeg -= 360f // 将大于360度的角度转换为小于360度的角度
//    }
    return angleDeg
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200)
@Composable
fun DraggableJoystickPreview(modifier: Modifier = Modifier) {
    RockerBox(
        modifier = Modifier,
        centerCircleRadius = 40f,
        canvasSize = 120.dp,
        centerCircleColor = MaterialTheme.colorScheme.primary,
        lineColor = MaterialTheme.colorScheme.onPrimary,
    ) { _, _ ->
    }
}