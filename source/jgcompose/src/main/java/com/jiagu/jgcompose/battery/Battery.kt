package com.jiagu.jgcompose.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * Battery
 *
 * @param modifier 装饰器
 * @param battery 电量
 * @param textColor 文本颜色
 * @param voltageRatio 电量比 0f ~ 1f
 */
@Composable
fun Battery(
    modifier: Modifier = Modifier,
    battery: String = "100",
    textColor: Color = Color.Black,
    voltageRatio: Float = 0.3f,
) {
    val batteryColor = when {
        voltageRatio < 0.3f -> Color.Red
        voltageRatio < 0.6f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .fillMaxHeight(0.04f)
                .align(alignment = Alignment.CenterHorizontally)
                .background(
                    color = batteryColor, shape = RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp)
                )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .border(width = 1.dp, color = batteryColor, shape = MaterialTheme.shapes.extraSmall)
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(voltageRatio)
                    .background(color = batteryColor, shape = MaterialTheme.shapes.extraSmall)
                    .align(Alignment.BottomCenter)
            )
        }
        Text(
            text = battery,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun BatteryPreview() {
    ComposeTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            Battery(
                battery = "30", modifier = Modifier
                    .width(30.dp)
                    .height(60.dp)
            )
        }
    }
}