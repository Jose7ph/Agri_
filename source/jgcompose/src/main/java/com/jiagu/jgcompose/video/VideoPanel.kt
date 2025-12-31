package com.jiagu.jgcompose.video

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 视频控制栏
 *
 * @param modifier 装饰器
 * @param buttonColor 按钮的颜色
 * @param textColor 文本颜色 (播放速度)
 * @param progress 进度条
 * @param playState 当前播放状态 默认false
 * @param playSpeed 当前播放速度 速度默认 1,2,4,8
 * @param sliderProgressHeight 滑块进度条高度
 * @param sliderButtonSize 滑块按钮大小
 * @param sliderButtonColor 滑块按钮颜色
 * @param sliderBackgroundColor 滑块背景色
 * @param sliderProgressColor 滑块进度条颜色
 * @param onProgressChange 进度条进度变化事件
 * @param onClickPlay 点击播放/暂停事件
 * @param onClickSpeed 点击速度事件
 * @param onClickStop 点击停止事件
 * @receiver
 */
@Composable
fun VideoPanel(
    modifier: Modifier = Modifier,
    buttonColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    progress: Float = 0f,
    playState: Boolean = false,
    playSpeed: Int = 1,
    sliderProgressHeight: Dp = 20.dp,
    sliderButtonSize: Float = 30f,
    sliderButtonColor: Color = Color.White,
    sliderBackgroundColor: Color = Color.LightGray,
    sliderProgressColor: Color = Color.Blue,
    onProgressChange: (Float) -> Unit = {},
    onClickPlay: (Boolean) -> Unit = {},
    onClickSpeed: (Int) -> Unit = {},
    onClickStop: () -> Unit = {},
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        //进度条
        VideoProgress(
            progress = progress,
            sliderProgressHeight = sliderProgressHeight,
            sliderButtonSize = sliderButtonSize,
            sliderButtonColor = sliderButtonColor,
            sliderBackgroundColor = sliderBackgroundColor,
            sliderProgressColor = sliderProgressColor,
            onChange = onProgressChange
        )
        //控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //停止
            Image(
                painter = painterResource(R.drawable.video_stop),
                contentDescription = "video stop",
                colorFilter = ColorFilter.tint(buttonColor),
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .noEffectClickable {
                        onClickStop()
                    }
            )

            //暂停/播放
            Image(
                painter = painterResource(if (playState) R.drawable.video_pause else R.drawable.video_play),
                contentDescription = "video controller",
                colorFilter = ColorFilter.tint(buttonColor),
                modifier = Modifier
                    .fillMaxHeight()
                    .noEffectClickable {
                        onClickPlay(!playState)
                    }
            )

            //倍速
            VideoSpeedButton(modifier = Modifier
                .fillMaxHeight(0.7f)
                .aspectRatio(1f),
                videoSpeed = playSpeed,
                fillColor = buttonColor,
                textColor = textColor,
                onSpeedChange = {
                    onClickSpeed(it)
                })
        }
    }
}

/**
 * 倍速按钮
 *
 * @param modifier
 * @param videoSpeed
 * @param textColor
 * @param fillColor
 * @param onSpeedChange
 * @receiver
 */
@Composable
private fun VideoSpeedButton(
    modifier: Modifier = Modifier,
    videoSpeed: Int,
    textColor: Color = Color.White,
    fillColor: Color = MaterialTheme.colorScheme.primary,
    onSpeedChange: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var componentSize by remember { mutableStateOf(Size.Zero) }

    // 动态字体计算（组合阶段）
    val (baseFontSize, maxTextWidth) = remember(componentSize) {
        with(density) {
            if (componentSize.width <= 0f) 18.sp to 0f
            else {
                val minDimension = componentSize.minDimension
                (minDimension / 2f).toSp() to minDimension * 0.8f
            }
        }
    }

    // 文本测量缓存（组合阶段）
    val adjustedLayout = remember(videoSpeed, baseFontSize) {
        val text = "${videoSpeed}X"
        val textStyle = TextStyle(fontSize = baseFontSize, color = textColor, fontWeight = W700)
        val layoutResult = textMeasurer.measure(text, textStyle)

        if (layoutResult.size.width > maxTextWidth && maxTextWidth > 0) {
            val scaledFontSize = baseFontSize * (maxTextWidth / layoutResult.size.width)
            textMeasurer.measure(text, textStyle.copy(fontSize = scaledFontSize))
        } else {
            layoutResult
        }
    }

    Canvas(modifier = modifier
        .clickable {
            onSpeedChange(
                when (videoSpeed) {
                    1 -> 2
                    2 -> 4
                    4 -> 8
                    else -> 1
                }
            )
        }
        .onGloballyPositioned {
            componentSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
        }) {
        if (componentSize.width <= 0f) return@Canvas

        // 绘制实心圆
        drawCircle(
            color = fillColor, radius = componentSize.minDimension / 2
        )

        // 绘制文本（绘制阶段）
        drawText(
            textLayoutResult = adjustedLayout, topLeft = Offset(
                center.x - adjustedLayout.size.width / 2, center.y - adjustedLayout.size.height / 2
            )
        )
    }
}

@Composable
private fun VideoProgress(
    progress: Float = 0f,
    sliderProgressHeight: Dp = 20.dp,
    sliderButtonSize: Float = 30f,
    sliderButtonColor: Color = Color.White,
    sliderBackgroundColor: Color = Color.LightGray,
    sliderProgressColor: Color = Color.Blue,
    onChange: (Float) -> Unit
) {
    var curProgress by remember { mutableFloatStateOf(progress) }
    var containerWidth by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) {
        curProgress = progress
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(sliderProgressHeight)
            .onGloballyPositioned {
                containerWidth = it.size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / containerWidth).coerceIn(0f, 1f)
                    curProgress = newProgress
                    Log.d("zhy", "detectTapGestures....")
                    onChange(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    val delta = dragAmount / containerWidth
                    curProgress = (curProgress + delta).coerceIn(0f, 1f)
                    onChange(curProgress)
                    Log.d("zhy", "detectHorizontalDragGestures....")
                    change.consume()
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // 绘制背景轨道
            drawLine(
                color = sliderBackgroundColor,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )

            // 绘制进度轨道
            drawLine(
                color = sliderProgressColor,
                start = Offset(0f, center.y),
                end = Offset(size.width * curProgress, center.y),
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )

            // 绘制圆形滑块
            drawCircle(
                color = sliderButtonColor,
                radius = sliderButtonSize,
                center = Offset(size.width * curProgress, center.y),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun VideoPanelPreview() {
    ComposeTheme {
        Column {
            VideoPanel(
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}