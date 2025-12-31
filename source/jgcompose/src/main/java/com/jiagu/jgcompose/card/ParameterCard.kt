package com.jiagu.jgcompose.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

@Composable
fun ParameterCard(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    textColor: Color = Color.Black
) {
    Column(
        modifier = modifier
            .border(width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.small)
            .clip(shape = MaterialTheme.shapes.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = text, color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ParameterCardPreview() {
    ComposeTheme {
        Column {
            ParameterCard(
                title = "参数", text = "1", modifier = Modifier
                    .width(120.dp)
                    .height(60.dp)
            )
        }
    }
}