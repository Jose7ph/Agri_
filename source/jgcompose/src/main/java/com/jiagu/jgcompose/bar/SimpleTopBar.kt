package com.jiagu.jgcompose.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.button.BackButton
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.theme.ComposeTheme

@Composable
fun SimpleTopBar(breakAction: () -> Unit, title: String, action: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .noEffectClickable(false) { },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BackButton {
                breakAction()
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier,
                textAlign = TextAlign.Center
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            color = Color.Transparent,
        ) {
            action()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 48)
@Composable
fun SimpleTopBarPreview() {
    ComposeTheme {
        SimpleTopBar(breakAction = {}, title = "title", action = {})
    }
}