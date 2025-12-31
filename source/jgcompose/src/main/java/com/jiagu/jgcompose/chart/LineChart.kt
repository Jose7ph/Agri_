package com.jiagu.jgcompose.chart

import android.R.attr.data
import android.graphics.Path
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.utils.toString
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 折线图
 *
 * @param modifier 修饰器
 * @param data 折现数据
 * @param selectedPoint 是否选择点 todo 功能还有问题
 * @param xLabelCount x轴坐标数量
 * @param yLabelCount y轴坐标数量
 * @param lineColor 折线颜色 默认红
 * @param showPoint 是否显示折线点
 * @param pointRadius 折线点大小
 * @param pointColor 折线点颜色 默认绿
 * @param axisXTextStyle x轴文本样式
 * @param axisYTextStyle y轴文本样式
 * @param onTapX 点击回调
 */
@Composable
fun LineChart(
    modifier: Modifier,
    data: List<Pair<Float, Float>>,
    selectedPoint: Pair<Float, Float>? = null,
    currentData: Pair<Float, Float>? = null,
    xMin: Float = 0f,
    xUnit: String = "",
    xLabelCount: Int = 5,
    yMin: Float = 0f,
    yUnit: String = "",
    yLabelCount: Int = 5,
    lineColor: Color = Color.Red,
    showPoint: Boolean = false,
    pointRadius: Float = 6f,
    pointColor: Color = Color.Green,
    axisXTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    axisYTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    onTapX: (Pair<Float, Float>) -> Unit,
) {
    if (data.isEmpty()) {
        Box(modifier = modifier)
        return
    }
    // Y轴最大值
    val maxY = remember(data) {
        data.maxOf { it.second }
    }
    // Y轴最小值
    val minY = yMin
    // X轴最大值
    val maxX = remember(data) {
        data.maxOf { it.first }
    }
    // X轴最小值
    val minX = xMin

    // 计算器
    var mCalculation by remember {
        mutableStateOf<LineChartCalculation?>(null)
    }
    val textMeasurer = rememberTextMeasurer()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            //坐标系
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 50.dp, top = 10.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { offset ->
                            // 点击反馈
                            mCalculation?.let { calculation ->
                                val x = calculation.parseX(offset.x)
                                val y = calculation.parseX(offset.y)
                                getYValueFromX(data, x) ?: return@let
                                onTapX.invoke(Pair(x, y))
                            }
                        })
                    }) {
                //x,y整体偏移量 用于防止文本显示不全
                val xOffset = 150
                val yOffset = 100
                // XY坐标轴上的指示条的长度
                val axisTipsLength = 6.dp.toPx()
                // 可供绘制图表的宽度和高度
                val chartW = size.width - axisTipsLength - xOffset
                val chartH = size.height - yOffset
                // 绘制X轴
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, chartH),
                    end = Offset(size.width - xOffset, chartH),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // 绘制X轴上面的指示条
                val spaceX = chartW / (xLabelCount - 1)
                for (i in 0 until xLabelCount) {
                    val x = spaceX * i + axisTipsLength
                    val xLabels =
                        genLabelData(min = minX.toInt(), max = maxX.toInt(), count = xLabelCount)
                    val textWidth =
                        textMeasurer.measure("${xLabels[i]}").size.width - axisTipsLength
                    //x刻度值
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${xLabels[i]}",
                        style = axisXTextStyle,
                        topLeft = Offset(
                            x - textWidth / 2, chartH + axisTipsLength
                        )
                    )
                    //x刻度线
                    drawLine(
                        color = Color.Black,
                        start = Offset(x, chartH),
                        end = Offset(x, size.height - yOffset + axisTipsLength),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                //x单位
                val xUnitWidth =
                    textMeasurer.measure(xUnit).size.width
                drawText(
                    textMeasurer = textMeasurer,
                    text = xUnit,
                    style = axisXTextStyle,
                    topLeft = Offset(
                        size.width - xUnitWidth, chartH + axisTipsLength
                    ),
                    maxLines = 1
                )

                // 绘制Y轴
                drawLine(
                    color = Color.Black,
                    start = Offset(axisTipsLength, 0f),
                    end = Offset(axisTipsLength, size.height - yOffset),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
                //y单位
                val yUnitWidth =
                    textMeasurer.measure(yUnit).size.width
                drawText(
                    textMeasurer = textMeasurer,
                    text = yUnit,
                    style = axisXTextStyle,
                    topLeft = Offset(
                        0f - yUnitWidth, -60f
                    ),
                    maxLines = 1
                )
                // 绘制Y轴上面的指示条
                val spaceY = chartH / (yLabelCount - 1)
                for (i in 0 until yLabelCount) {
                    val y = spaceY * i
                    val yLabels = genLabelData(
                        min = minY.toInt(),
                        max = maxY.toInt(),
                        count = yLabelCount
                    ).apply {
                        reverse()
                    }
                    val textSize = textMeasurer.measure("${yLabels[i]}").size
                    val textWidth = textSize.width
                    val textHeight = textSize.height
                    //y刻度值
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${yLabels[i]}",
                        style = axisYTextStyle,
                        topLeft = Offset(0f - textWidth, y - (textHeight / 3))
                    )
                    //y刻度线
                    drawLine(
                        color = Color.Black,
                        start = Offset(axisTipsLength, y),
                        end = Offset(0f, y),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 绘制曲线
                // 创建计算器
                val calculation = LineChartCalculation(
                    chartW, axisTipsLength, maxX, minX, chartH, 0f, maxY, minY
                )
                mCalculation = calculation
                // 创建路径
                val path = Path()
                //处理数据 根据x值排序
                val sortData = data.sortedBy { it.first }
                // 移动到开始点
//                val controlX = axisTipsLength
                // 移动到第一个点
                val controlX = calculation.getX(sortData[0].first)
                val controlY = calculation.getY(sortData[0].second)
                path.moveTo(controlX, controlY)
                sortData.forEach {
                    val currentX = calculation.getX(it.first)
                    val currentY = calculation.getY(it.second)
                    path.lineTo(currentX, currentY)
                }
                // 绘制路径
                drawPath(
                    path.asComposePath(),
                    color = lineColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
                // 连线到右侧底部
                path.lineTo(size.width - xOffset, chartH)
                // 连线到左侧底部
                path.lineTo(axisTipsLength, chartH)
                // 闭合路径
                path.close()
                // 绘制曲线下方的渐变色
                drawPath(
                    path.asComposePath(), brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = lineColor.alpha / 2), Color.Transparent
                        )
                    )
                )
                selectedPoint?.let {
                    // 画当前选择线
                    val selectXp = calculation.getX(selectedPoint.first)
                    val selectYp = calculation.getY(selectedPoint.second)
                    val pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f), phase = 10f
                    )
                    drawLine(
                        color = Color.Blue,
                        start = Offset(selectXp, 0f),
                        end = Offset(selectXp, selectYp),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                    drawLine(
                        color = Color.Blue,
                        start = Offset(0f + axisTipsLength, selectYp),
                        end = Offset(selectXp, selectYp),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                }
                //路径点
                if (showPoint) {
                    sortData.forEach {
                        drawCircle(
                            color = pointColor,
                            radius = pointRadius,
                            center = Offset(
                                x = calculation.getX(it.first),
                                y = calculation.getY(it.second)
                            )
                        )
                    }
                }
                currentData?.let {
                    drawCircle(
                        color = Color.Red,
                        radius = 10f,
                        center = Offset(
                            x = calculation.getX(it.first),
                            y = calculation.getY(it.second)
                        )
                    )
                    val currentDataValue = "(${currentData.first.roundToInt()},${currentData.second.roundToInt()})"
                    //当前点的值
                    drawText(
                        textMeasurer = textMeasurer,
                        text = currentDataValue,
                        style = axisYTextStyle,
                        topLeft = Offset(
                            calculation.getX(it.first) + 5,
                            calculation.getY(it.second) - 50
                        )
                    )
                }
            }
        }
    }
}

/**
 * 折线图
 *
 * @param modifier 修饰器
 * @param data 折现数据
 * @param selectedPoint 是否选择点 todo 功能还有问题
 * @param xLabelCount x轴坐标数量
 * @param yLabelCount y轴坐标数量
 * @param lineColor 折线颜色 默认红
 * @param showPoint 是否显示折线点
 * @param pointRadius 折线点大小
 * @param pointColor 折线点颜色 默认绿
 * @param axisXTextStyle x轴文本样式
 * @param axisYTextStyle y轴文本样式
 * @param onTapX 点击回调
 */
class PumpLineChartData(
    val x: Float,
    val y: Float,
    val xAxisUnit: String,
    val yAxisUnit: String,
    val third: Float? = null,
    val thirdAxisUnit: String? = null,
) {
    override fun toString(): String {
        return String.format(Locale.US, "(%.2f,%.2f,%.2f)", x, y, third)
    }
}

@Composable
fun PumpLineChart(
    modifier: Modifier,
    data: List<PumpLineChartData>,
    selectedPoint: Pair<Float, Float>? = null,
    xLabelCount: Int = 10,
    lineColor: Color = Color.Red,
    showPoint: Boolean = false,
    pointRadius: Float = 6f,
    pointColor: Color = Color.Green,
    axisXTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    axisYTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    onTapX: (Pair<Float, Float>) -> Unit,
) {
    val yLabelCount = 10
    if (data.isEmpty()) {
        Box(modifier = modifier)
        return
    }
    // Y轴最大值
    val maxY = remember(data) {
        data.maxOf { it.y }
    }
    // Y轴最小值
    val minY = 0f
    // X轴最大值
    val maxX = remember(data) {
        data.maxOf { it.x }
    }
    // X轴最小值
    val minX = 0f

    // 计算器
    var mCalculation by remember {
        mutableStateOf<LineChartCalculation?>(null)
    }

    val calcMaxY = if (maxY in 10f..20f) 20f else if (maxY in 0f..10f) 10f else maxY
    val listX = data.sortedBy { it.x }
    val listAvgY = mutableListOf<Float>()//y是从上往下画的
    val avgY = calcMaxY / yLabelCount.toFloat()
    for (i in 0 until yLabelCount + 1) {
        listAvgY.add(avgY * i)
    }
    val listY = listAvgY.sortedByDescending { it }
//    val listY = data.sortedByDescending { it.y }
    Log.v(
        "shero", "list:${listX.toTypedArray().contentToString()} " +
                "listY:${listY.toTypedArray().contentToString()}"
    )

    val textMeasurer = rememberTextMeasurer()
    val xAxisUnit = data.first().xAxisUnit
    val yAxisUnit = data.first().yAxisUnit
    val thirdAxisUnit = data.first().thirdAxisUnit
//    val xTextWidth = textMeasurer.measure(xAxisUnit)
//    val yTextWidth = textMeasurer.measure(yAxisUnit)
//    Log.v("shero", "chart ${xTextWidth.size.width} ${yTextWidth.size.width}")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(
                modifier = Modifier
                    .width(50.dp)
                    .padding(top = 5.dp, end = 5.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    text = yAxisUnit,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                //坐标系
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 36.dp, top = 10.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = { offset ->
                                // 点击反馈
                                mCalculation?.let { calculation ->
                                    val x = calculation.parseX(offset.x)
                                    val y = calculation.parseX(offset.y)
                                    val yValue = getPumpYValueFromX(data, x) ?: return@let
                                    Log.d("zhy", "tap: x:${x},y:${y},yvalue:${yValue}")
                                    onTapX.invoke(Pair(x, y))
                                }
                            })
                        }) {
                    //x,y整体偏移量 用于防止文本显示不全
                    val xOffset = 100
                    val yOffset = 100
                    // XY坐标轴上的指示条的长度
                    val axisTipsLength = 6.dp.toPx()
                    // 可供绘制图表的宽度和高度
                    val chartW = size.width - axisTipsLength - xOffset
                    val chartH = size.height - yOffset
                    // 绘制X轴
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, chartH),
                        end = Offset(size.width - xOffset, chartH),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // 绘制X轴上面的指示条
                    val spaceX = chartW / (xLabelCount - 1)
                    for (i in 0 until xLabelCount) {
                        val v = listX[i]
                        val x = spaceX * i + axisTipsLength
                        val xLabels = genLabelData(
                            min = minX.toInt(),
                            max = maxX.toInt(),
                            count = xLabelCount
                        )
                        val textWidth =
                            textMeasurer.measure("${xLabels[i]}").size.width - axisTipsLength
                        //x刻度值
                        drawText(
                            textMeasurer = textMeasurer,
                            text = v.x.toString(0),
                            style = axisXTextStyle,
                            topLeft = Offset(
                                x - textWidth / 2, chartH + axisTipsLength
                            )
                        )
                        if (v.third != null) {
                            //x轴第三个值
                            drawText(
                                textMeasurer = textMeasurer,
                                text = v.third.toString(0),
                                style = axisXTextStyle,
                                topLeft = Offset(x - textWidth / 2, chartH + axisTipsLength + 36)
                            )
                        }
                        //x刻度线
                        drawLine(
                            color = Color.Black,
                            start = Offset(x, chartH),
                            end = Offset(x, size.height - yOffset + axisTipsLength),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    // 绘制Y轴
                    drawLine(
                        color = Color.Black,
                        start = Offset(axisTipsLength, 0f),
                        end = Offset(axisTipsLength, size.height - yOffset),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // 绘制Y轴上面的指示条
                    val spaceY = chartH / (yLabelCount)
                    for (i in 0 until yLabelCount + 1) {
                        val y = spaceY * i
                        //y刻度线
                        drawLine(
                            color = Color.Black,
                            start = Offset(axisTipsLength, y),
                            end = Offset(0f, y),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        if (i >= listY.size) continue
                        val v = listY[i]

                        genLabelData(
                            min = minY.toInt(),
                            max = maxY.toInt(),
                            count = yLabelCount
                        ).apply { reverse() }
                        val t = v.toString(1)
                        val textSize = textMeasurer.measure("99.99").size
                        val textWidth = textSize.width
                        val textHeight = textSize.height
                        //y刻度值
                        drawText(
                            textMeasurer = textMeasurer,
                            text = t,
                            style = axisYTextStyle,
                            topLeft = Offset(0f - textWidth, y - (textHeight / 3))
                        )
                    }

                    // 绘制曲线
                    // 创建计算器
                    val calculation = LineChartCalculation(
                        chartW, axisTipsLength, listX.last().x, listX.first().x,
                        chartH, 0f, listY.first(), listY.last()
                    )
                    mCalculation = calculation
                    // 创建路径
                    val path = Path()
                    //处理数据 根据x值排序
                    val sortData = data.sortedBy { it.x }
                    // 移动到开始点
//                val controlX = axisTipsLength
                    // 移动到第一个点
                    val controlX = calculation.getX(sortData[0].x)
                    val controlY = calculation.getY(sortData[0].y)
                    path.moveTo(controlX, controlY)
                    sortData.forEach {
                        val currentX = calculation.getX(it.x)
                        val currentY = calculation.getY(it.y)
                        path.lineTo(currentX, currentY)
                    }
                    // 绘制路径
                    drawPath(
                        path.asComposePath(),
                        color = lineColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    // 连线到右侧底部
                    path.lineTo(size.width - xOffset, chartH)
                    // 连线到左侧底部
                    path.lineTo(axisTipsLength, chartH)
                    // 闭合路径
                    path.close()
                    // 绘制曲线下方的渐变色
                    drawPath(
                        path.asComposePath(), brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = lineColor.alpha / 2), Color.Transparent
                            )
                        )
                    )
                    selectedPoint?.let {
                        // 画当前选择线
                        val selectXp = calculation.getX(selectedPoint.first)
                        val selectYp = calculation.getY(selectedPoint.second)
                        val pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10f, 10f), phase = 10f
                        )
                        drawLine(
                            color = Color.Blue,
                            start = Offset(selectXp, 0f),
                            end = Offset(selectXp, selectYp),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = pathEffect
                        )
                        drawLine(
                            color = Color.Blue,
                            start = Offset(0f + axisTipsLength, selectYp),
                            end = Offset(selectXp, selectYp),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = pathEffect
                        )
                    }
                    //路径点
                    if (showPoint) {
                        sortData.forEach {
                            drawCircle(
                                color = pointColor,
                                radius = pointRadius,
                                center = Offset(
                                    x = calculation.getX(it.x),
                                    y = calculation.getY(it.y)
                                )
                            )
                        }
                    }
//                drawLine(
//                    color = Color(0xFFFF574D),
//                    start = Offset(0f + axisTipsLength, -chartH),
//                    end = Offset(selectXp, chartH),
//                    strokeWidth = 1.dp.toPx(),
//                    cap = StrokeCap.Round
//                )
                }
            }
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .padding(top = 5.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    text = xAxisUnit,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    text = thirdAxisUnit ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * 通过X轴数据获取Y轴数据
 * @param data List<Pair<, Float>>
 * @param x
 * @return Float?
 */
private fun getPumpYValueFromX(data: List<PumpLineChartData>, x: Float): Float? {
    // 先抱有侥幸心里尝试下是不是一下子就点到了真实数据
    val selectIndex = data.indexOfFirst { it.x == x }
    if (selectIndex >= 0) {
        // 真就那么巧，牛B
        return data[selectIndex].y
    } else {
        // 没那么幸运，那就得找到一前一后的真实点，然后做虚拟数据了
        val leftIndex = data.indexOfLast { it.x < x }
        // 没有找到前面的数据，说明点击超出图表范围了
        if (leftIndex !in 0 until data.size - 1) return null
        val leftPoint = data[leftIndex]
        val rightPoint = data[leftIndex + 1]
        // 计算选择的时间在两个点的时间中的百分比
        val per = (x - leftPoint.x) / (rightPoint.x - leftPoint.x)
        // 计算Y轴数据
        // 返回
        return (rightPoint.y - leftPoint.y) * per + leftPoint.y
    }
}

/**
 * 通过X轴数据获取Y轴数据
 * @param data List<Pair<, Float>>
 * @param x
 * @return Float?
 */
private fun getYValueFromX(data: List<Pair<Float, Float>>, x: Float): Float? {
    // 先抱有侥幸心里尝试下是不是一下子就点到了真实数据
    val selectIndex = data.indexOfFirst { it.first == x }
    if (selectIndex >= 0) {
        // 真就那么巧，牛B
        return data[selectIndex].second
    } else {
        // 没那么幸运，那就得找到一前一后的真实点，然后做虚拟数据了
        val leftIndex = data.indexOfLast { it.first < x }
        // 没有找到前面的数据，说明点击超出图表范围了
        if (leftIndex !in 0 until data.size - 1) return null
        val leftPoint = data[leftIndex]
        val rightPoint = data[leftIndex + 1]
        // 计算选择的时间在两个点的时间中的百分比
        val per = (x - leftPoint.first) / (rightPoint.first - leftPoint.first)
        // 计算Y轴数据
        // 返回
        return (rightPoint.second - leftPoint.second) * per + leftPoint.second
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun LineChartPreview() {
//    val list = listOf(
//        Pair(1000.0f, 0.0f),
//        Pair(1113.09f, 50f),
//        Pair(1250.0f, 0.0f),
//        Pair(1500.0f, 50.0f),
//        Pair(2000.0f, 1500f)
//    )
    var point by remember {
        mutableStateOf(Pair(1500F, 50f))
    }

    val pumpList = listOf(
        PumpLineChartData(0f, 0f, "rpm", "L/min", 0f, "pwm"),
        PumpLineChartData(15000.0f, 9.54f, "rpm", "L/min", 5000f, "pwm"),
        PumpLineChartData(12000.0f, 8.16f, "rpm", "L/min", 5000f, "pwm"),
        PumpLineChartData(9000f, 7.08f, "rpm", "L/min", 4000f, "pwm"),
        PumpLineChartData(7000f, 5.94f, "rpm", "L/min", 3000f, "pwm"),
        PumpLineChartData(4000f, 3.78f, "rpm", "L/min", 2000f, "pwm"),
        PumpLineChartData(3000f, 2.82f, "rpm", "L/min", 1000f, "pwm"),
    )
    //起始点
    ComposeTheme {
//        LineChart(modifier = Modifier, data = list,
//            selectedPoint = point,
//            onTapX = {
//                point = it
//                Log.d("zhy", "point: ${point}")
//            })

        PumpLineChart(
            modifier = Modifier, data = pumpList,
            selectedPoint = null,
            xLabelCount = pumpList.size,
            onTapX = {
                point = it
                Log.d("zhy", "point: ${point}")
            })
    }
}

/**
 * 图表数据位置计算
 * @property width Float
 * @property widthOffset Float
 * @property maxX
 * @property minX
 * @property height Float
 * @property heightOffset Float
 * @property maxY Float
 * @property minY Float
 * @constructor
 */
private class LineChartCalculation(
    val width: Float,
    val widthOffset: Float,
    val maxX: Float,
    val minX: Float,
    val height: Float,
    val heightOffset: Float,
    val maxY: Float,
    val minY: Float,
) {

    fun getX(value: Float): Float {
        return (width * ((value - minX) / (maxX - minX))) + widthOffset
    }

    fun parseX(value: Float): Float {
        return (((value - widthOffset) / width) * (maxX - minX)) + minX
    }

    fun getY(value: Float): Float {
//        Log.d(
//            "zhy",
//            "value:${value},width: ${width}," + "widthOffset:${widthOffset}, " + "maxX:${maxX}," + "minX:${minX}," + "height:${height}," + "heightOffset:${heightOffset}," + "maxY:${maxY}," + "minY:${minY}"
//        )
        return height - (height * ((value - minY) / (maxY - minY))) + heightOffset
    }

    fun parseY(value: Float): Float {
        return ((value - heightOffset - height) / height) * (maxY - minY) + minY
    }
}

/**
 * 生成坐标轴数据
 *
 * @param max 最大值
 * @param count 轴点数量
 * @return 返回的轴数组
 */
private fun genLabelData(
    min: Int,
    max: Int,
    count: Int,
): IntArray {
    return when {
        count < 1 -> return intArrayOf(min)
        count == 1 -> return intArrayOf(min, max)
        else -> {
            // 计算步长
            val step = (max - min) / (count - 1)
            IntArray(count) { index ->
                when (index) {
                    0 -> min
                    count - 1 -> max
                    else -> index * step + min
                }
            }
        }
    }
}
