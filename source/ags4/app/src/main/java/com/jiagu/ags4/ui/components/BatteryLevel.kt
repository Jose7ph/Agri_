package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.ui.theme.ComposeTheme

@Composable
fun BatteryLevel(
    width: Dp, height: Dp, battery: Int
) {
    Row(modifier = Modifier.size(width, height)) {
        BatteryBox(
            battery = battery, modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(4.dp)
                .align(Alignment.CenterVertically)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun BatteryBox(modifier: Modifier = Modifier, battery: Int) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)
                )
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((battery / 100.0).toFloat())
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.End)
            )
        }
        Text(
            text = battery.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun BatteryVerticalBox(
    modifier: Modifier = Modifier,
    battery: String = "100",
    voltPercentHeight: Float = 100f,
    fontSize: TextUnit = 10.sp,
    batteryColor: Color = MaterialTheme.colorScheme.primary,
    textColor:Color = Color.Black
) {
    var maxHeight = voltPercentHeight
    if (0f > maxHeight) {
        maxHeight = 1f
    }
    if (maxHeight > 1f) {
        maxHeight = 0f
    }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .border(
                        1.dp, batteryColor, RoundedCornerShape(4.dp)
                    )
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(maxHeight)
                        .background(batteryColor)
                        .align(Alignment.Bottom),
                )
            }


        }
        Text(
            text = battery,
            color = textColor,
            fontSize = fontSize,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

    }

}


@Preview(showBackground = true, heightDp = 200)
@Composable
fun testBatteryBox() {
    ComposeTheme {
        Column {
            BatteryLevel(width = 60.dp, height = 20.dp, 50)
            BatteryVerticalBox(
                battery = "50", modifier = Modifier
                    .height(50.dp)
                    .width(30.dp)
            )
        }
    }
}