package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.battery.Battery
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun BatteryPreview() {
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