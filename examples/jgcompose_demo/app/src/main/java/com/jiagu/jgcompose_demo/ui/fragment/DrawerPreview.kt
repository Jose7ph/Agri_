package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllDrawersPreview() {
    var isShow by remember { mutableStateOf(true) }
    ComposeTheme {
        Column {
            Drawer(
                context = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.33f)
                    )
                },
                onShow = {
                    isShow = true
                }, onClose = {
                    isShow = false
                }, isShow = isShow,
                color = Color.Gray
            )
        }
    }
}
