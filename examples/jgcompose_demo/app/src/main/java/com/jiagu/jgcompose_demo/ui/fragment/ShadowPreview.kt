package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllShadowPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Different Height ShadowFrame", style = MaterialTheme.typography.titleMedium)
                
                // light (2dp)
                ShadowFrame(
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp),
                    elevation = 2.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("2dp Shadow", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // normal (8dp)
                ShadowFrame(
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp),
                    elevation = 8.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("8dp Shadow", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // dark (16dp)
                ShadowFrame(
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp),
                    elevation = 16.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("16dp Shadow", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Different shape previews
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("ShadowFrame with Different Shapes", style = MaterialTheme.typography.titleMedium)
                
                // Small corner radius
                ShadowFrame(
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.small,
                    shadowShape = MaterialTheme.shapes.small,
                    elevation = 8.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Small Radius", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // Medium corner radius
                ShadowFrame(
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    shadowShape = MaterialTheme.shapes.medium,
                    elevation = 8.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Medium Radius", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // Large corner radius
                ShadowFrame(
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    shadowShape = RoundedCornerShape(20.dp),
                    elevation = 8.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Large Radius", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}