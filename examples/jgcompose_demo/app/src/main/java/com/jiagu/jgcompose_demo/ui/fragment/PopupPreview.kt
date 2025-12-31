package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun PromptPopupPreview() {
    ComposeTheme {
        Column {
            PromptPopup(title = "Notice",
                content = "This is a notice",
                onDismiss = {},
                onConfirm = {})
        }
    }
}