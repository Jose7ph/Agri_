package com.jiagu.jgcompose.chart

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.utils.toString
import kotlin.math.ceil

data class BarData(
    val name: String,
    val value: Float,
    val color: Color
)

@Composable
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 24.dp,
    chartHeight: Dp = 200.dp,
    textColor: Color = Color.Black,
    showName: Boolean = true,
    showValue: Boolean = true
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    Row(
        modifier = modifier
            .height(chartHeight)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { bar ->
            val barHeight = bar.value / maxValue
            Column(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // 顶部文字（数值）
                if (showValue){
                    Text(
                        text = bar.value.toString(),
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (barHeight < 1) {
                    Spacer(
                        modifier = Modifier.weight(1f - barHeight)
                    )
                }
                if (barHeight > 0) {
                    // 柱子
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(barHeight)
                            .background(bar.color)
                    )
                }

                if (showName) {
                    Text(
                        text = bar.name,
                        modifier = Modifier.padding(top = 4.dp),
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }// 底部标签

            }
        }
    }
}

@Composable
fun GridBarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 30.dp,
    chartHeight: Dp = 100.dp,
    textColor: Color = Color.Unspecified,
    columns: Int = 15,
    showName: Boolean = true,
    showValue: Boolean = true
) {
    // 根据数据数量计算行数和列数
    val dataSize = data.size
    val rows = ceil(dataSize.toDouble() / columns).toInt()
    val gridHeight = rows * (chartHeight.value + 20f) // 20f 为行间距
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.height(gridHeight.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(data.size) { index ->
            val item = data[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showValue) {
                    Text(
                        text = item.value.toString(0),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Canvas(
                    modifier = Modifier
                        .width(barWidth)
                        .height(chartHeight)
                ) { 
                    val barHeight = size.height * (item.value / 100f)
                    drawRect(
                        color = item.color,
                        topLeft = Offset(0f, size.height - barHeight),
                        size = Size(size.width, barHeight)
                    )
                }
                if (showName) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 640, heightDp = 360, showBackground = false)
@Composable
fun BarChartSample() {
    val data = listOf(
        BarData("A", 30f, Color.Red),
        BarData("B", 80f, Color.Green),
        BarData("C", 55f, Color.Blue),
        BarData("D", 90f, Color.Magenta),
        BarData("E", 40f, Color.Cyan),
        BarData("B", 80f, Color.Green),
        BarData("C", 55f, Color.Blue),
        BarData("D", 90f, Color.Magenta),
        BarData("E", 40f, Color.Cyan),
        BarData("B", 80f, Color.Green),
        BarData("C", 55f, Color.Blue),
        BarData("D", 90f, Color.Magenta),
        BarData("E", 40f, Color.Cyan),
        BarData("B", 80f, Color.Green),
        BarData("C", 55f, Color.Blue),
        BarData("D", 90f, Color.Magenta),
        BarData("E", 40f, Color.Cyan)
    )

    GridBarChart(
        data = data,
        modifier = Modifier.fillMaxWidth(),
        barWidth = 32.dp,
        chartHeight = 80.dp,
        textColor = Color.White
    )
}