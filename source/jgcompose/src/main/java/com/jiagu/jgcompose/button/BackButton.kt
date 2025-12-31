package com.jiagu.jgcompose.button

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .aspectRatio(1f), onClick = onClick
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "back",
            tint = Color.White
        )

    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun BackButtonPreview() {
    ComposeTheme {
        Column{
            BackButton(){}
        }
    }
}