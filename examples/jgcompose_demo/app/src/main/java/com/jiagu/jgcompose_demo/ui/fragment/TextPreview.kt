package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllTextPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AutoScrollingText preview - normal text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                AutoScrollingText(
                    text = "This is a long text used to demonstrate the auto-scrolling text effect. When the text exceeds the container width, it will automatically scroll.",
                    width = 200.dp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // AutoScrollingText preview - styled text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val annotatedString = buildAnnotatedString {
                    append("Styled text: ")
                    pushStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold))
                    append("Important info")
                    pop()
                    append(" Normal info")
                }
                
                AutoScrollingText(
                    text = annotatedString,
                    width = 200.dp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Text with different colors and alignments
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AutoScrollingText(
                    text = "Left-aligned text",
                    width = 150.dp,
                    color = Color.Blue,
                    textAlign = TextAlign.Start
                )
                
                AutoScrollingText(
                    text = "Center text",
                    width = 150.dp,
                    color = Color.Green,
                    textAlign = TextAlign.Center
                )
                
                AutoScrollingText(
                    text = "Right-aligned text",
                    width = 150.dp,
                    color = Color.Red,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}