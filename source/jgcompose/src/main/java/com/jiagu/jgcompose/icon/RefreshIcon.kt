package com.jiagu.jgcompose.icon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.jiagu.jgcompose.theme.ComposeTheme

@Composable
fun RefreshIcon(modifier: Modifier = Modifier, iconColor: Color = Color.Black) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Filled.Refresh,
            contentDescription = "refresh",
            tint = iconColor
        )
    }
}


@Preview()
@Composable
private fun RefreshIconPreview() {
    ComposeTheme {
        Column {
            RefreshIcon()
        }
    }
}