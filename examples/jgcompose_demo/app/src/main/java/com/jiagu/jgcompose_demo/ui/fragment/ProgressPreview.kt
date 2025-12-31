package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.progress.Progress
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllProgressPreviews() {
    ComposeTheme {
        Column {
            Progress(
                modifier = Modifier
                    .padding(30.dp)
                    .fillMaxWidth(0.5f)
                    .height(30.dp),
                progress = 0.75f
            )
        }
    }
}
