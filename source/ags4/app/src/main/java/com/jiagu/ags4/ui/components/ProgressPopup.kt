package com.jiagu.ags4.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.jgcompose.popup.ScreenPopup

@Composable
fun ProgressPopup(
    progressHeight: Dp = 10.dp,
    progressWidth: Dp = 100.dp,
    progress: Float = 0f,
    text: String = "",
    progressBarMaxHeight: Dp = 40.dp,
    popupWidth: Dp,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    showCancel: Boolean = false,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        width = popupWidth, content = {
            Column(
                modifier = Modifier
//                    .fillMaxWidth(0.5f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = textStyle
                    )
                }
                ProgressBar(
                    modifier = Modifier
                        .height(progressBarMaxHeight)
                        .fillMaxWidth(),
                    progress = progress,
                    progressWidth = progressWidth,
                    progressHeight = progressHeight
                )
            }
        }, showCancel = showCancel, showConfirm = false,
        onDismiss = {
            onDismiss()
        }

    )
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    progressHeight: Dp = 10.dp,
    progressWidth: Dp = 100.dp,
    progressBackgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    progressBorderColor: Color = MaterialTheme.colorScheme.primary,
) {
    val progressValue by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f))
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = progressBackgroundColor, shape = MaterialTheme.shapes.large
                )
                .border(
                    width = 1.dp,
                    color = progressBorderColor,
                    shape = MaterialTheme.shapes.large
                )
                .clip(shape = MaterialTheme.shapes.large)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progressValue)
                    .height(progressHeight)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.large
                    )
            )
        }
//        Text(
//            text = "${(progressValue * 100).toString(0)}%",
//            color = Color.Black,
//            style = MaterialTheme.typography.labelLarge
//        )
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun ProgressBarPopupPreview(modifier: Modifier = Modifier) {
    var boolean by remember {
        mutableStateOf(true)
    }
    ComposeTheme {
        Column {
            Button(
                onClick = { boolean = !boolean }, modifier = Modifier
                    .width(100.dp)
                    .height(40.dp)
            ) {
                Text(text = "progress", color = Color.White)
            }
            if (boolean) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)
                        .background(color = Color.White)
                ) {
                    ProgressPopup(
                        progress = 0.4f,
                        text = "aaaaa",
                        popupWidth = 100.dp,
                        showCancel = true,
                        onDismiss = {})
                }
            }
        }
    }
}