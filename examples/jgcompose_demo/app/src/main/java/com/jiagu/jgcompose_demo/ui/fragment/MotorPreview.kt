package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jiagu.jgcompose.motor.Motor
import com.jiagu.jgcompose.motor.MotorInfo
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllMotorsPreview() {
    ComposeTheme {
        Column {
            Motor(
                motorInfo = MotorInfo(
                    numberString = "M1",
                    state = "ON",
                    speed = "1500",
                    current = "2.5",
                    voltage = "12.1",
                    temperature = "45",
                    percent = 75,
                    duration = "30",
                    enabled = true
                )
            )
        }
    }
}
