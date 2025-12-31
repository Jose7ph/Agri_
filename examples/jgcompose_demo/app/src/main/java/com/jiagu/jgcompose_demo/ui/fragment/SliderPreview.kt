package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagutech.jgcompose.ui.slider.Slider

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllSliderPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Basic slider preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var sliderResult1 by remember { mutableStateOf(false) }
                
                Slider(
                    sliderTitle = "Swipe to unlock",
                    sliderWidth = 300.dp,
                    sliderHeight = 40.dp,
                    onSuccess = { result ->
                        sliderResult1 = result
                    }
                )
                
                if (sliderResult1) {
                    Text("Slider completed!")
                }
            }
            
            // Slider with long press function preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var sliderResult2 by remember { mutableStateOf(false) }
                var longPressState by remember { mutableStateOf(false) }
                
                Slider(
                    sliderTitle = "Long press to slide",
                    sliderWidth = 300.dp,
                    sliderHeight = 40.dp,
                    longPressState = longPressState,
                    longPressTime = 2.0f,
                    onSuccess = { result ->
                        sliderResult2 = result
                    }
                )
                
                if (!sliderResult2) {
                    Text("Click the button below to start long press sliding")
                    Button(
                        onClick = { longPressState = true }
                    ) {
                        Text("Start long press sliding")
                    }
                } else {
                    Text("Long press slider completed!")
                }
            }
            
            // Slider with different sizes preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var sliderResult3 by remember { mutableStateOf(false) }
                
                Slider(
                    sliderTitle = "Large slider",
                    sliderWidth = 400.dp,
                    sliderHeight = 60.dp,
                    onSuccess = { result ->
                        sliderResult3 = result
                    }
                )
                
                if (sliderResult3) {
                    Text("Large slider completed!")
                }
            }
        }
    }
}