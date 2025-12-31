package com.jiagu.jgcompose.rocker

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.counter.ClickStatusEnum
import com.jiagu.jgcompose.ext.longPressListener
import com.jiagu.jgcompose.theme.ComposeTheme


@Composable
fun StraightDirectionRocker(
    modifier: Modifier = Modifier,
    centerContent: @Composable BoxScope.() -> Unit = {},
    rockerColor: Color = Color.Gray,
    onTopClick: () -> Unit,
    onTopClickFinish: () -> Unit = {},
    onBottomClick: () -> Unit,
    onBottomClickFinish: () -> Unit = {},
    onLeftClick: () -> Unit,
    onLeftClickFinish: () -> Unit = {},
    onRightClick: () -> Unit,
    onRightClickFinish: () -> Unit = {},
) {
    val straightDirectionImageSize = 0.4f
    val centerMaxSize = 0.2f
    var clickType by remember { mutableStateOf(ClickStatusEnum.TAP_END) }
    LaunchedEffect(clickType) {
        Log.d("zhy", "clickType: ${clickType}")
    }
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            // 绘制圆形边框
            drawCircle(
                color = Color.Gray,
                radius = size.minDimension / 2, // 半径取最小值边的一半，留10像素边距
                style = Stroke(width = 4f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize(centerMaxSize)
                .align(alignment = Alignment.Center)
        ) {
            centerContent()
        }
        Image(
            painter = painterResource(R.drawable.angle_arrow_top),
            contentDescription = "top",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxHeight(straightDirectionImageSize)
                .align(alignment = Alignment.TopCenter)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            clickType = ClickStatusEnum.TAP_START
                            if(clickType == ClickStatusEnum.TAP_START){
                                onTopClick()
                            }
                        },
                        done = {
                            clickType = ClickStatusEnum.TAP_END
                            if(clickType == ClickStatusEnum.TAP_END){
                                onTopClickFinish()
                            }
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_bottom),
            contentDescription = "bottom",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxSize(straightDirectionImageSize)
                .align(alignment = Alignment.BottomCenter)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onBottomClick()
                        },
                        done = {
                            onBottomClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_left),
            contentDescription = "left",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxSize(straightDirectionImageSize)
                .align(alignment = Alignment.CenterStart)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onLeftClick()
                        },
                        done = {
                            onLeftClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_right),
            contentDescription = "right",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxSize(straightDirectionImageSize)
                .align(alignment = Alignment.CenterEnd)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onRightClick()
                        },
                        done = {
                            onRightClickFinish()
                        })
                }
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DiagonalDirectionRocker(
    modifier: Modifier = Modifier,
    centerContent: @Composable BoxScope.() -> Unit = {},
    rockerColor: Color = Color.Gray,
    onTopClick: () -> Unit,
    onTopClickFinish: () -> Unit = {},
    onBottomClick: () -> Unit,
    onBottomClickFinish: () -> Unit = {},
    onLeftClick: () -> Unit,
    onLeftClickFinish: () -> Unit = {},
    onRightClick: () -> Unit,
    onRightClickFinish: () -> Unit = {},
    onTopLeftClick: () -> Unit,
    onTopLeftClickFinish: () -> Unit = {},
    onTopRightClick: () -> Unit,
    onTopRightClickFinish: () -> Unit = {},
    onBottomLeftClick: () -> Unit,
    onBottomLeftClickFinish: () -> Unit = {},
    onBottomRightClick: () -> Unit,
    onBottomRightClickFinish: () -> Unit = {},
) {
    val straightDirectionImageSize = 0.4f
    val diagonalDirectionImageSize = 0.15f
    val centerMaxSize = 0.2f
    BoxWithConstraints(modifier = modifier) {
        // 计算基础尺寸单位（取父容器宽高的最小值）
        val baseSize = min(maxWidth, maxHeight)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            // 绘制圆形边框
            drawCircle(
                color = Color.Gray,
                radius = size.minDimension / 2, // 半径取最小值边的一半，留10像素边距
                style = Stroke(width = 4f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize(centerMaxSize)
                .align(alignment = Alignment.Center)
        ) {
            centerContent()
        }
        Image(
            painter = painterResource(R.drawable.angle_arrow_top),
            contentDescription = "top",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxHeight(straightDirectionImageSize)
                .align(alignment = Alignment.TopCenter)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onTopClick()
                        },
                        done = {
                            onTopClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_bottom),
            contentDescription = "bottom",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxSize(straightDirectionImageSize)
                .align(alignment = Alignment.BottomCenter)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onBottomClick()
                        },
                        done = {
                            onBottomClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_left),
            contentDescription = "left",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxSize(straightDirectionImageSize)
                .align(alignment = Alignment.CenterStart)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onLeftClick()
                        },
                        done = {
                            onLeftClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_right),
            contentDescription = "right",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .fillMaxSize(straightDirectionImageSize)
                .align(alignment = Alignment.CenterEnd)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onRightClick()
                        },
                        done = {
                            onRightClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_top_left),
            contentDescription = "top_left",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .offset(
                    x = baseSize * diagonalDirectionImageSize,
                    y = baseSize * diagonalDirectionImageSize
                )
                .fillMaxSize(diagonalDirectionImageSize)
                .align(alignment = Alignment.TopStart)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onTopLeftClick()
                        },
                        done = {
                            onTopLeftClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_top_right),
            contentDescription = "top_right",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .offset(
                    x = -baseSize * diagonalDirectionImageSize,
                    y = baseSize * diagonalDirectionImageSize
                )
                .fillMaxSize(diagonalDirectionImageSize)
                .align(alignment = Alignment.TopEnd)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onTopRightClick()
                        },
                        done = {
                            onTopRightClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_bottom_left),
            contentDescription = "bottom_left",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .offset(
                    x = baseSize * diagonalDirectionImageSize,
                    y = -baseSize * diagonalDirectionImageSize
                )
                .fillMaxSize(diagonalDirectionImageSize)
                .align(alignment = Alignment.BottomStart)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onBottomLeftClick()
                        },
                        done = {
                            onBottomLeftClickFinish()
                        })
                }
        )
        Image(
            painter = painterResource(R.drawable.angle_arrow_bottom_right),
            contentDescription = "bottom_right",
            colorFilter = ColorFilter.tint(rockerColor),
            modifier = Modifier
                .offset(
                    x = -baseSize * diagonalDirectionImageSize,
                    y = -baseSize * diagonalDirectionImageSize
                )
                .fillMaxSize(diagonalDirectionImageSize)
                .align(alignment = Alignment.BottomEnd)
                .pointerInput(Unit) {
                    longPressListener(
                        progress = {
                            onBottomRightClick()
                        },
                        done = {
                            onBottomRightClickFinish()
                        })
                }
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DirectionRockerPreview() {
    ComposeTheme {
        Column {
            StraightDirectionRocker(
                modifier = Modifier.size(210.dp),
                onTopClick = {},
                onTopClickFinish = {},
                onLeftClick = {},
                onLeftClickFinish = {},
                onRightClick = {},
                onRightClickFinish = {},
                onBottomClick = {},
                onBottomClickFinish = {},
            )
            DiagonalDirectionRocker(
                modifier = Modifier.size(110.dp),
                onTopClick = {},
                onTopClickFinish = {},
                onLeftClick = {},
                onLeftClickFinish = {},
                onRightClick = {},
                onRightClickFinish = {},
                onBottomClick = {},
                onBottomClickFinish = {},
                onTopLeftClick = {},
                onTopLeftClickFinish = {},
                onTopRightClick = {},
                onTopRightClickFinish = {},
                onBottomLeftClick = {},
                onBottomLeftClickFinish = {},
                onBottomRightClick = {},
                onBottomRightClickFinish = {},
            )
        }
    }
}