package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.chart.BarChart
import com.jiagu.jgcompose.chart.BarData
import com.jiagu.jgcompose.chart.LineChart
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllChartsPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val barData = listOf(
                BarData("A", 30f, Color.Red),
                BarData("B", 80f, Color.Green),
                BarData("C", 55f, Color.Blue),
            )
            BarChart(
                data = barData,
                modifier = Modifier.weight(1f).height(200.dp)
            )

            val lineData = listOf(
                Pair(0f, 10f),
                Pair(1f, 20f),
                Pair(2f, 15f),
                Pair(3f, 25f),
                Pair(4f, 22f)
            )
            LineChart(
                modifier = Modifier.weight(1f).height(200.dp),
                data = lineData,
                onTapX = {}
            )
        }
    }
}
