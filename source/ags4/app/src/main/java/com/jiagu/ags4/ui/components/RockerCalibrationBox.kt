package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * topLineFillRate、bottomLineFillRate、leftLineFillRate、rightLineFillRate值范围 100-200
 * 未操作情况下都是150
 * centerCircleRadius 中心圆点大小
 */
@Composable
fun RockerCalibrationBox(
    modifier: Modifier = Modifier,
    centerCircleRadius: Float = 10f,
    width: Dp = 150.dp,
    height: Dp = 150.dp,
    lineBackgroundColor: Color = Color.Gray,
    topLineFillRate: Float = 0f,
    bottomLineFillRate: Float = 0f,
    leftLineFillRate: Float = 0f,
    rightLineFillRate: Float = 0f,
    lineThickness: Dp = 2.dp
) {

    val radiusHeight = height / 2
    val radiusWidth = width / 2
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        //rate text
        val topRateText = (topLineFillRate * 100).toInt().toString() + "%"
        val rightRateText = (rightLineFillRate * 100).toInt().toString() + "%"
        val bottomRateText = (bottomLineFillRate * 100).toInt().toString() + "%"
        val leftRateText = (leftLineFillRate * 100).toInt().toString() + "%"
        val rateTextFontSize = 10.sp

        //top rate text
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        ) {
            Text(text = topRateText, style = MaterialTheme.typography.labelSmall)
        }
        //right rate text
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 25.dp)
        ) {
            Text(text = rightRateText, style = MaterialTheme.typography.labelSmall)
        }
        //bottom rate text
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp)
        ) {
            Text(text = bottomRateText, style = MaterialTheme.typography.labelSmall)
        }
        //left rate text
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-25).dp)
        ) {
            Text(text = leftRateText, style = MaterialTheme.typography.labelSmall)
        }
        //X轴
        Surface(
            modifier = Modifier
                .width(width)
                .height(lineThickness)
                .align(Alignment.Center),
            color = lineBackgroundColor,
            shape = RoundedCornerShape(4.dp),
        ) {}
        //Y轴
        Surface(
            modifier = Modifier
                .width(lineThickness)
                .height(height)
                .align(Alignment.Center),
            color = lineBackgroundColor,
            shape = RoundedCornerShape(4.dp),
        ) {}

        //top
        //topLineFillRate > 1f 显示红色 长度固定1f
        val topColor = if (topLineFillRate > 1f) Color.Red else Color.Green
        val topLineFillRateValue = if (topLineFillRate > 1f) 1f else topLineFillRate
        val topFill = radiusHeight * topLineFillRateValue
        Surface(
            modifier = Modifier
                .width(lineThickness)
                .height(topFill)
                .align(Alignment.Center)
                .offset(0.dp, topFill * -1 / 2), // 计算top起始点
            color = topColor,
            shape = RoundedCornerShape(4.dp),
        ) {}
        //right
        //rightLineFillRate > 1f 显示红色 长度固定1f
        val rightColor = if (rightLineFillRate > 1f) Color.Red else Color.Green
        val rightLineFillRateValue = if (rightLineFillRate > 1f) 1f else rightLineFillRate
        val rightFill = radiusWidth * rightLineFillRateValue
        Surface(
            modifier = Modifier
                .width(rightFill)
                .height(lineThickness)
                .offset(rightFill / 2, 0.dp) // 计算right起始点
                .align(Alignment.Center),
            color = rightColor,
            shape = RoundedCornerShape(4.dp),
        ) {}
        //bottom
        val bottomColor = if (bottomLineFillRate > 1f) Color.Red else Color.Green
        val bottomLineFillRateValue = if (bottomLineFillRate > 1f) 1f else bottomLineFillRate
        val bottomFill = radiusHeight * bottomLineFillRateValue
        Surface(
            modifier = Modifier
                .width(lineThickness)
                .height(bottomFill)
                .offset(0.dp, bottomFill / 2) // 计算bottom起始点
                .align(Alignment.Center),
            color = bottomColor,
            shape = RoundedCornerShape(4.dp),
        ) {}
        //left
        val leftColor = if (leftLineFillRate > 1f) Color.Red else Color.Green
        val leftLineFillRateValue = if (leftLineFillRate > 1f) 1f else leftLineFillRate
        val leftFill = radiusWidth * leftLineFillRateValue
        Surface(
            modifier = Modifier
                .width(leftFill)
                .height(lineThickness)
                .offset(leftFill * -1 / 2, 0.dp)// 计算left起始点
                .align(Alignment.Center),
            color = leftColor,
            shape = RoundedCornerShape(4.dp),
        ) {}

        Canvas(
            modifier = Modifier
                .width(width)
                .height(height)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            // 计算圆心坐标
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2

            //圆心
            drawCircle(
                color = Color.Green, // 圆形的颜色
                center = Offset(centerX, centerY), // 圆形的中心点
                radius = centerCircleRadius
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 200, widthDp = 200)
@Composable
fun RockerCalibrationBoxPreview(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp), contentAlignment = Alignment.Center
    ) {
        RockerCalibrationBox()
//        CrossHandleGraph()
    }
}