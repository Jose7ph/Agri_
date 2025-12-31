package com.jiagu.jgcompose.motor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.progress.Progress
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * 电机信息
 * status: 电机状态
 * speed: 电机转速
 * current: 电机电流
 * voltage: 电机电压
 * temperature: 电机温度
 * percent: 输出百分比
 * percentRate: 输出百分比 0-1f
 * duration: 已用时间 分钟
 */
class MotorInfo(
    val numberString: String,
    val state: String,
    var throttleSource: String = "",
    val speed: String,
    val current: String,
    val voltage: String,
    val temperature: String,
    val percent: Short,
    val percentRate: Float = percent / 100f,
    val duration: String,
    val stateString: String = "",
    val number: Int = 0,
    val enabled: Boolean = true
) {
    override fun toString(): String {
        return "motorInfo(numberString='$numberString'," +
                " state='$state'," +
                " throttleSource='$throttleSource'," +
                " speed='$speed'," +
                " current='$current'," +
                " voltage='$voltage'," +
                " temperature='$temperature'," +
                " percent=$percent," +
                " duration='$duration'," +
                " stateString='$stateString'," +
                " number=$number)"
    }
}

@Composable
fun Motor(
    modifier: Modifier = Modifier,
    motorInfo: MotorInfo,
    onNameClick: () -> Unit = {},
    showCheck: Boolean = true,
    onCheckClick: () -> Unit = {}
) {
    val extendedColors = LocalExtendedColors.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //name
        Button(
            onClick = onNameClick,
            modifier = Modifier.size(34.dp),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (motorInfo.enabled) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled,
                disabledContainerColor = extendedColors.buttonDisabled
            ),
        ) {
            Text(
                text = motorInfo.numberString,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //progress
            Progress(
                progress = motorInfo.percentRate,
                throughTime = 500,
                borderColor = if (motorInfo.enabled) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled
            )
            //param
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val textStyle = MaterialTheme.typography.titleSmall
                val textColor = MaterialTheme.colorScheme.primary
                if (motorInfo.enabled) {
                    //电压
                    if (motorInfo.voltage.isNotBlank()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = motorInfo.voltage + " V",
                                modifier = Modifier.fillMaxWidth(),
                                color = textColor,
                                style = textStyle
                            )
                        }
                    }

                    //电流
                    if (motorInfo.current.isNotBlank()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = motorInfo.current + " A",
                                modifier = Modifier.fillMaxWidth(),
                                color = textColor,
                                style = textStyle
                            )
                        }
                    }
                    //温度
                    if (motorInfo.temperature.isNotBlank()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = motorInfo.temperature + "°",
                                modifier = Modifier.fillMaxWidth(),
                                color = textColor,
                                style = textStyle
                            )
                        }
                    }
                    //转速
                    if (motorInfo.speed.isNotBlank()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = motorInfo.speed + " rpm",
                                modifier = Modifier.fillMaxWidth(),
                                color = textColor,
                                style = textStyle
                            )
                        }
                    }
                    //时间
                    if (motorInfo.duration.isNotBlank()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = motorInfo.duration + "min",
                                modifier = Modifier.fillMaxWidth(),
                                color = textColor,
                                style = textStyle
                            )
                        }
                    }
                }
            }
        }
        if (showCheck) {
            Button(
                onClick = onCheckClick,
                modifier = Modifier
                    .width(80.dp)
                    .height(30.dp),
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (motorInfo.enabled) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled,
                    disabledContainerColor = extendedColors.buttonDisabled
                ),
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.inspection),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun MotorPreview() {
    ComposeTheme {
        Column {
            Motor(
                motorInfo = MotorInfo(
                    numberString = "M${0 + 1}",
                    state = "1",
                    speed = "2",
                    current = "3",
                    voltage = "4",
                    temperature = "5",
                    percent = 0,
                    percentRate = 0 / 100f,
                    duration = "6",
                    number = 0,
                    enabled = false
                )
            )
        }
    }
}