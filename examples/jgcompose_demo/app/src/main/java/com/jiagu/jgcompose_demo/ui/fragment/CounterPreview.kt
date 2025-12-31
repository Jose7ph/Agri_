package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.counter.RangeSliderCounter
import com.jiagu.jgcompose.counter.SliderCounter
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllCountersPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var floatCounterValue by remember { mutableStateOf(5f) }
            FloatCounter(
                modifier = Modifier
                    .width(150.dp)
                    .height(40.dp),
                number = floatCounterValue,
                min = 0f,
                max = 10f,
                step = 0.5f,
                fraction = 1,
                onValueChange = { floatCounterValue = it }
            )

            var rangeSliderValue by remember { mutableStateOf(20f..80f) }
            RangeSliderCounter(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                value = rangeSliderValue,
                valueRange = 0f..100f,
                step = 1f,
                titleContent = {
                    Text("Range: ${it.start} - ${it.endInclusive}")
                },
                onChange = { rangeSliderValue = it }
            )

            var sliderValue by remember { mutableStateOf(50f) }
            SliderCounter(
                modifier = Modifier.height(40.dp),
                number = sliderValue,
                valueRange = 0f..100f,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { sliderValue = it }
            )
        }
    }
}
