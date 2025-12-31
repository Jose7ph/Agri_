package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllContainersPreview() {
    ComposeTheme {
        MainContent(
            title = "Preview Title",
            breakAction = {},
            barAction = { Text(text = "Action") }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = "Main content goes here.")
            }
        }
    }
}
