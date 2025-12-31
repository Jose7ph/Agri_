package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
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
import com.jiagu.jgcompose.screen.ScreenColumn
import com.jiagu.jgcompose.screen.ScreenSearchColumn
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllScreenPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ScreenColumn
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                var showScreenColumn by remember { mutableStateOf(false) }
                
                if (showScreenColumn) {
                    ScreenColumn(
                        content = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text("ScreenColumn")
                                Text("Any Content")
                            }
                        },
                        buttons = {
                            Text("search")
                        },
                        onCancel = {
                            showScreenColumn = false
                        },
                        onConfirm = {
                            showScreenColumn = false
                        }
                    )
                } else {
                    Button(
                        onClick = { showScreenColumn = true }
                    ) {
                        Text("show ScreenColumn")
                    }
                }
            }
            
            // ScreenSearchColumn
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                var showScreenSearchColumn by remember { mutableStateOf(false) }
                
                if (showScreenSearchColumn) {
                    ScreenSearchColumn(
                        content = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text("ScreenSearchColumn")
                                Text("any content")
                            }
                        },
                        onSearch = { query ->
                            // logic
                        },
                        onCancel = {
                            showScreenSearchColumn = false
                        },
                        onConfirm = {
                            showScreenSearchColumn = false
                        }
                    )
                } else {
                    Button(
                        onClick = { showScreenSearchColumn = true }
                    ) {
                        Text("show ScreenSearchColumn")
                    }
                }
            }
        }
    }
}