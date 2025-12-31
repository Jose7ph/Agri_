package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.video.VideoPanel

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllVideoPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Basic video control panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                var progress by remember { mutableFloatStateOf(0.3f) }
                var isPlaying by remember { mutableStateOf(false) }
                var speed by remember { mutableStateOf(1) }
                
                VideoPanel(
                    modifier = Modifier
                        .background(Color.Gray)
                        .fillMaxWidth()
                        .height(100.dp),
                    progress = progress,
                    playState = isPlaying,
                    playSpeed = speed,
                    onProgressChange = { progress = it },
                    onClickPlay = { isPlaying = it },
                    onClickSpeed = { speed = it },
                    onClickStop = { 
                        isPlaying = false
                        progress = 0f
                    }
                )
            }
            
            // Video control panel with different styles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                var progress2 by remember { mutableFloatStateOf(0.7f) }
                var isPlaying2 by remember { mutableStateOf(true) }
                var speed2 by remember { mutableStateOf(2) }
                
                VideoPanel(
                    modifier = Modifier
                        .background(Color.DarkGray)
                        .fillMaxWidth()
                        .height(100.dp),
                    progress = progress2,
                    playState = isPlaying2,
                    playSpeed = speed2,
                    buttonColor = Color.Red,
                    sliderProgressColor = Color.Green,
                    onProgressChange = { progress2 = it },
                    onClickPlay = { isPlaying2 = it },
                    onClickSpeed = { speed2 = it },
                    onClickStop = { 
                        isPlaying2 = false
                        progress2 = 0f
                    }
                )
            }
            
            // Video control panel with custom styles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                var progress3 by remember { mutableFloatStateOf(0.5f) }
                var isPlaying3 by remember { mutableStateOf(false) }
                var speed3 by remember { mutableStateOf(4) }
                
                VideoPanel(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxWidth()
                        .height(100.dp),
                    progress = progress3,
                    playState = isPlaying3,
                    playSpeed = speed3,
                    sliderProgressHeight = 30.dp,
                    sliderButtonSize = 40f,
                    onProgressChange = { progress3 = it },
                    onClickPlay = { isPlaying3 = it },
                    onClickSpeed = { speed3 = it },
                    onClickStop = { 
                        isPlaying3 = false
                        progress3 = 0f
                    }
                )
            }
        }
    }
}