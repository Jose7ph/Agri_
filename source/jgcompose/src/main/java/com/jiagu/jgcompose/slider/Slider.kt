package com.jiagutech.jgcompose.ui.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 滑块box
 *
 * @param sliderTitle 滑块内部文本
 * @param sliderWidth 滑块宽度
 * @param sliderHeight 滑块高度
 * @param shape 滑块圆角
 * @param onSuccess 滑动结果
 */
@Composable
fun Slider(
    sliderTitle: String = "",
    sliderWidth: Dp = 500.dp,
    sliderHeight: Dp = 50.dp,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    onSuccess: (Boolean) -> Unit
) {
    var positionX by remember {
        mutableIntStateOf(0)
    }
    //滑块宽高比 宽度是1.5 * height
    val sliderRatio = 1.5f
    // 滑块轨道宽度转换为像素
    val sliderWidthPx = with(LocalDensity.current) { sliderWidth.toPx() }
    // 滑块宽度转换为像素
    val sliderHeightPx = with(LocalDensity.current) { sliderHeight.toPx() * sliderRatio }
    // 计算滑块的最大可拖动距离（轨道宽度 - 滑块宽度）
    val maxDragDistance = (sliderWidthPx - sliderHeightPx).toInt()

    var success by remember {
        mutableStateOf(false)
    }
    var positionChange by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(key1 = success) {
        onSuccess(success)
    }
    //滑块轨道
    Box(
        modifier = Modifier
            .width(sliderWidth)
            .height(sliderHeight)
            .background(color = Color.Gray, shape = shape),
    ) {
        //滑块文本 当滑块移动时不显示文本
        if (positionChange == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            ) {
                AutoScrollingText(
                    text = sliderTitle,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        //滑块已经到头
        if (success) {
            Box(
                modifier = Modifier
                    .height(sliderHeight)
                    .aspectRatio(sliderRatio)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "success",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(sliderHeight - 10.dp)
                        .background(color = Color.Transparent)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .height(sliderHeight)
                    .aspectRatio(sliderRatio)
                    .offset { IntOffset(positionX, 0) }
                    .pointerInput(null) {
                        detectDragGestures(
                            onDragStart = {
                                //拖动开始处理 没完成先归0 已完成则直接挪动滑块到末尾
                                positionX = if (success) {
                                    maxDragDistance
                                } else {
                                    0
                                }
                            },
                            onDragEnd = {
                                //松手触发 判断结果
                                //移动到最后修改success = true 否则为false
                                if (positionX == maxDragDistance) {
                                    success = true
                                    positionX = maxDragDistance
                                } else {
                                    positionX = 0
                                }
                                positionChange = positionX
                            },
                            onDrag = { change, dragAmount ->
                                //拖动时处理
                                if (!success) {
                                    val newPosition = positionX + dragAmount.x.toInt()
                                    positionX = if (newPosition < 0) {
                                        0
                                    } else if (newPosition > maxDragDistance) {
                                        success = true
                                        maxDragDistance
                                    } else {
                                        newPosition
                                    }
                                    positionChange = newPosition
                                    change.consume()
                                }
                            })
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )

            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(sliderHeight)
                            .offset((-10).dp, 0.dp)
                            .background(color = Color.Transparent)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(sliderHeight)
                            .offset(10.dp, 0.dp)
                            .background(color = Color.Transparent)
                    )
                }
            }
        }
    }
}

/**
 * 滑块box
 * 该组件可通过外部状态控制变化
 *
 * @param sliderTitle 滑块内部文本
 * @param sliderWidth 滑块宽度
 * @param sliderHeight 滑块高度
 * @param shape 滑块圆角
 * @param longPressState 长按状态
 * @param longPressTime 长按时间 秒
 * @param onSuccess 滑动结果
 */
@Composable
fun Slider(
    sliderTitle: String = "",
    sliderWidth: Dp = 500.dp,
    sliderHeight: Dp = 50.dp,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    longPressState: Boolean,
    longPressTime: Float = 2.0f,
    onSuccess: (Boolean) -> Unit
) {
    var positionX by remember {
        mutableIntStateOf(0)
    }
    //滑块宽高比 宽度是1.5 * height
    val sliderRatio = 1.5f
    // 滑块轨道宽度转换为像素
    val sliderWidthPx = with(LocalDensity.current) { sliderWidth.toPx() }
    // 滑块宽度转换为像素
    val sliderHeightPx = with(LocalDensity.current) { sliderHeight.toPx() * sliderRatio }
    // 计算滑块的最大可拖动距离（轨道宽度 - 滑块宽度）
    val maxDragDistance = (sliderWidthPx - sliderHeightPx).toInt()

    var success by remember {
        mutableStateOf(false)
    }
    var positionChange by remember {
        mutableIntStateOf(0)
    }

    // 创建一个 Animatable 并初始化
    val animatable = remember { Animatable(0f) }
    // 使用 animateFloatAsState 获取动画的当前状态
    val progressValue by animateFloatAsState(
        targetValue = animatable.value,
    )
    LaunchedEffect(key1 = longPressState) {
        if (!success) {
            animatable.snapTo(0f)
        }
        if (longPressState && !success) {
            animatable.animateTo(
                maxDragDistance.toFloat(),
                animationSpec = tween((longPressTime * 1000).toInt())
            )//2s 进度条走完
            success = true
        }
    }

    LaunchedEffect(key1 = success) {
        onSuccess(success)
    }
    //滑块轨道
    Box(
        modifier = Modifier
            .width(sliderWidth)
            .height(sliderHeight)
            .background(color = Color.Gray, shape = shape),
    ) {
        //滑块文本 当滑块移动时不显示文本
        if (positionChange == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            ) {
                AutoScrollingText(
                    text = sliderTitle,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        //滑块已经到头
        if (success) {
            Box(
                modifier = Modifier
                    .height(sliderHeight)
                    .aspectRatio(sliderRatio)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "success",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(sliderHeight - 10.dp)
                        .background(color = Color.Transparent)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .height(sliderHeight)
                    .aspectRatio(sliderRatio)
                    .then(
                        if (longPressState) {
                            Modifier.offset { IntOffset(progressValue.toInt(), 0) }
                        } else {
                            Modifier.offset { IntOffset(positionX, 0) }
                        }
                    )
                    .pointerInput(null) {
                        detectDragGestures(
                            onDragStart = {
                                //拖动开始处理 没完成先归0 已完成则直接挪动滑块到末尾
                                positionX = if (success) {
                                    maxDragDistance
                                } else {
                                    0
                                }
                            },
                            onDragEnd = {
                                //松手触发 判断结果
                                //移动到最后修改success = true 否则为false
                                if (positionX == maxDragDistance) {
                                    success = true
                                    positionX = maxDragDistance
                                } else {
                                    positionX = 0
                                }
                                positionChange = positionX
                            },
                            onDrag = { change, dragAmount ->
                                //拖动时处理
                                if (!success) {
                                    val newPosition = positionX + dragAmount.x.toInt()
                                    positionX = if (newPosition < 0) {
                                        0
                                    } else if (newPosition > maxDragDistance) {
                                        success = true
                                        maxDragDistance
                                    } else {
                                        newPosition
                                    }
                                    positionChange = newPosition
                                    change.consume()
                                }
                            })
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )

            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(sliderHeight)
                            .offset((-10).dp, 0.dp)
                            .background(color = Color.Transparent)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(sliderHeight)
                            .offset(10.dp, 0.dp)
                            .background(color = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun SliderPreview() {
    ComposeTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Slider(sliderTitle = "aa", onSuccess = {})
        }
    }
}
