package com.jiagu.jgcompose.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 进度条
 *
 * @param modifier 装饰器
 * @param progress 进度值
 * @param progressHeight 进度条高度
 * @param throughTime 动画延迟时间 单位 ms
 * @param progressBackgroundColor 进度条背景色 默认白
 */
@Composable
fun Progress(
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    progressHeight: Dp = 10.dp,
    throughTime: Int = 0,
    progressBackgroundColor: Color = Color.White,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val progressValue by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(throughTime),
        label = "animate float"
    )
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
                    color = borderColor,
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
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ProgressPreview() {
    ComposeTheme {
        Column {
            Progress(progress = 0.9f, throughTime = 20000)
        }
    }
}