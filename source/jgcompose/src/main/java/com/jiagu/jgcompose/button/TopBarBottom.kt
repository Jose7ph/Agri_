package com.jiagu.jgcompose.button

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * Top bar bottom
 *
 * @param text 文本
 * @param buttonColor 按钮颜色
 * @param onClick 点击回调
 */
@Composable
fun TopBarBottom(
    text: String,
    buttonColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .widthIn(min = 80.dp, max = 150.dp)
            .height(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        shape = MaterialTheme.shapes.small
    ) {
        AutoScrollingText(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun TopBarBottomPreview() {
    ComposeTheme {
        Column {
            TopBarBottom(text = "按钮") {}
        }
    }
}