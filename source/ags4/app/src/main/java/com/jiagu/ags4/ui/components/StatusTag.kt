package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText

enum class TagType {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    ERROR
}

@Composable
fun StatusTag(type: TagType, title: String) {
    val backgroundColor = when (type) {
        TagType.ERROR -> MaterialTheme.colorScheme.error
        TagType.PRIMARY -> MaterialTheme.colorScheme.primary
        TagType.TERTIARY -> MaterialTheme.colorScheme.tertiary
        TagType.SECONDARY -> MaterialTheme.colorScheme.secondary
    }
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            .widthIn(max = 60.dp)
            .padding(
                horizontal = 4.dp, vertical = 2.dp
            ),
    ) {
        AutoScrollingText(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun Test() {
    StatusTag(type = TagType.PRIMARY, title = "primary")
}