package com.jiagu.jgcompose.rocker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 摇杆操作说明
 *
 * @property left 左推
 * @property right 右推
 * @property top 上推
 * @property bottom 下推
 */
class RockerText(val left: String, val right: String, val top: String, val bottom: String)

/**
 * Rocker
 *
 * @param rockerTitle 摇杆名称 一般是 L/R
 * @param rockerSize 摇杆大小 用于控制摇杆图案大小
 * @param rockerText 摇杆文本名称 上/下/左/右 默认:T/B/L/R
 * @param rockerValues 摇杆值 size为4的float array, arr[0]:左摇杆-上下 ,arr[1]:左摇杆-左右 ,arr[2]: 右摇杆-上下,arr[3]:右摇杆-左右
 * @param isLeft 是否左摇杆 true 左摇杆， false 右摇杆
 */
@Composable
fun Rocker(
    rockerTitle: String,
    rockerSize: Dp = 160.dp,
    rockerText: RockerText,
    rockerValues: FloatArray?,
    isLeft: Boolean = true,
) {
    val density = LocalDensity.current
    //摇杆盒子大小 用于显示摇杆边框
    val rockerBoxSize = rockerSize + 40.dp
    val thickness = 2.dp
    val textPadding = 6.dp
    //圆心大小
    val centerCircleSize = 15.dp
    //中心圆半径
    val centerRadius = centerCircleSize / 2
    //坐标长度
    val coordinateSize = rockerBoxSize * 0.6f
    //圆心位置
    val centerPointPos = (coordinateSize / 2) - centerRadius
    val textStyle = MaterialTheme.typography.bodySmall
    //摇杆数据
    var x = centerPointPos
    var y = centerPointPos
//    // 上:-1 ~ 0   下 :1 ~ 0  左 :1 ~ 0  右 :1 ~ 0
    if (rockerValues != null && rockerValues.size == 4) {
        if (isLeft) {//左摇杆
            var leftX = rockerValues[1]
            var leftY = rockerValues[0]
            if (leftX > 0) {
                x += (centerPointPos * leftX) + centerRadius
            } else if (leftX < 0) {
                leftX = -leftX
                x -= (centerPointPos * leftX) + centerRadius
            } else {
                x = centerPointPos
            }
            if (leftY > 0) {
                y += (centerPointPos * leftY) + centerRadius
            } else if (leftY < 0) {
                leftY = -leftY
                y -= (centerPointPos * leftY) + centerRadius
            } else {
                y = centerPointPos
            }
        } else { //右摇杆
            var rightX = rockerValues[3]
            var rightY = rockerValues[2]
            if (rightX > 0) {
                x += (x * rightX) + centerRadius
            } else if (rightX < 0) {
                rightX = -rightX
                x -= (x * rightX) + centerRadius
            } else {
                x = centerPointPos
            }
            if (rightY > 0) {
                y += (y * rightY) + centerRadius
            } else if (rightY < 0) {
                rightY = -rightY
                y -= (y * rightY) + centerRadius
            } else {
                y = centerPointPos
            }
        }
    }

    //虚线相关
    val dashedLineStyle = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), phase = 1f)
    val dashedLineColor = MaterialTheme.colorScheme.primary
    val dashedLineWidth = 3f
    val dashedLine = @Composable {
        val xDashedLineEnd = centerPointPos - x
        val yDashedLineEnd = centerPointPos - y
        val xDashedLineEndFloat = with(density) { xDashedLineEnd.toPx() }
        val yDashedLineEndFloat = with(density) { yDashedLineEnd.toPx() }

        Canvas(
            modifier = Modifier
                .offset(
                    x = x + centerRadius,
                    y = y + centerRadius
                )
        ) {
            drawLine(
                color = dashedLineColor,
                start = Offset(x = xDashedLineEndFloat, y = 0f),
                end = Offset(x = 0f, y = 0f),
                strokeWidth = dashedLineWidth,
                pathEffect = dashedLineStyle
            )
            drawLine(
                color = dashedLineColor,
                start = Offset(x = 0f, y = yDashedLineEndFloat),
                end = Offset(x = 0f, y = 0f),
                strokeWidth = dashedLineWidth,
                pathEffect = dashedLineStyle
            )
        }
    }

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(rockerBoxSize)
                .border(
                    width = 1.dp, shape = CircleShape, color = Color.LightGray
                ), contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(rockerBoxSize * 0.96f)
                    .border(
                        width = 1.dp,
                        shape = CircleShape,
                        color = Color.Gray
                    ), contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = textPadding)
                        .align(Alignment.TopCenter)
                ) {
                    AutoScrollingText(
                        text = rockerText.top,
                        style = textStyle,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(bottom = textPadding)
                        .align(Alignment.BottomCenter)
                ) {
                    AutoScrollingText(
                        text = rockerText.bottom,
                        style = textStyle,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = textPadding / 2)
                        .align(Alignment.CenterStart)
                        .offset(x = 0.dp, y = (-10).dp)
                ) {
                    AutoScrollingText(
                        text = rockerText.left,
                        style = textStyle,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(end = textPadding / 2)
                        .align(Alignment.CenterEnd)
                        .offset(x = 0.dp, y = (-10).dp)

                ) {
                    AutoScrollingText(
                        text = rockerText.right,
                        style = textStyle,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier.size(coordinateSize)
                ) {
                    dashedLine()
                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(centerCircleSize)
                            .clip(shape = CircleShape)
                            .background(color = MaterialTheme.colorScheme.primary)
                            .zIndex(1f)
                    )

                    HorizontalDivider(
                        thickness = thickness,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(coordinateSize),
                        color = Color.Gray
                    )
                    VerticalDivider(
                        thickness = thickness,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(coordinateSize),
                        color = Color.Gray
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            RockerImage(title = rockerTitle)
        }
    }
}

/**
 * 摇杆图片
 *
 * @param title 标题
 */
@Composable
private fun RockerImage(title: String) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Color.White, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RockerPreview() {
    ComposeTheme {
        Column {
            Rocker(
                rockerText = RockerText("L", "R", "T", "B"),
                rockerTitle = "L",
                rockerValues = null
            )
        }
    }
}