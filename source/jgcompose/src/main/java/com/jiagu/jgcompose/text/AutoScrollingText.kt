package com.jiagu.jgcompose.text

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme
import kotlinx.coroutines.delay


@Composable
fun AutoScrollingText(
    modifier: Modifier = Modifier,
    text: String,
    width: Dp = Dp.Infinity,
    color: Color = Color.Black,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = TextStyle.Default
) {
    val scrollState = rememberScrollState()
    val scrollSpeed = 2 // 速度，单位: 像素每帧

    LaunchedEffect(key1 = text) {
        while (true) {
            // 滚动到末尾
            while (scrollState.maxValue - scrollState.value > scrollSpeed) {
                scrollState.scrollBy(scrollSpeed.toFloat())
                delay(16L) // 约 60 fps 的刷新率
            }

            // 在这里暂停片刻，然后滚动回最左侧
            delay(1000L) // 在末端停留一段时间
            scrollState.scrollTo(0)
            delay(500L) // 在开头停留一段时间
        }
    }

    Row(modifier = Modifier.width(width)) {
        Text(
            text = text,
            modifier = modifier
                .padding(horizontal = 4.dp)
                .horizontalScroll(scrollState),
            maxLines = 1,
            color = color,
            textAlign = textAlign,
            style = style
        )
    }
}

@Composable
fun AutoScrollingText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    width: Dp = Dp.Infinity,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = TextStyle.Default
) {
    val scrollState = rememberScrollState()
    val scrollSpeed = 2 // 速度，单位: 像素每帧

    LaunchedEffect(key1 = text) {
        while (true) {
            // 滚动到末尾
            while (scrollState.maxValue - scrollState.value > scrollSpeed) {
                scrollState.scrollBy(scrollSpeed.toFloat())
                delay(16L) // 约 60 fps 的刷新率
            }

            // 在这里暂停片刻，然后滚动回最左侧
            delay(1000L) // 在末端停留一段时间
            scrollState.scrollTo(0)
            delay(500L) // 在开头停留一段时间
        }
    }

    Row(modifier = Modifier.width(width)) {
        Text(
            text = text,
            modifier = modifier
                .padding(horizontal = 4.dp)
                .horizontalScroll(scrollState),
            maxLines = 1,
            textAlign = textAlign,
            style = style
        )
    }
}

@Preview(widthDp = 640, heightDp = 360)
@Composable
fun PreviewAutoScrollingText() {
    ComposeTheme {
        Column {
            AutoScrollingText(text = "Hello, World!", width = 20.dp)
            AutoScrollingText(
                text = "Hello, World!",
                width = 40.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }
}

