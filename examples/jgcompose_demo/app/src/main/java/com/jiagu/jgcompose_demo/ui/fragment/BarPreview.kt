package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.bar.SimpleTopBar
import com.jiagu.jgcompose_demo.LocalNavController

@Preview(showBackground = true, widthDp = 360, heightDp = 48)
@Composable
fun PreviewSimpleTopBar() {
    val navController = LocalNavController.current
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(36.dp)) {
        SimpleTopBar(breakAction = {
            navController.popBackStack()
        }, title = "Preview Title", action = {
            Button(modifier = Modifier.height(32.dp), onClick = {  }) {
                Text(text = "Action")
            }
        })
    }
}
