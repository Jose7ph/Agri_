package com.jiagu.jgcompose.radar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 雷达位置 enum
 *
 * @constructor Create empty Radar position enum
 */
enum class RadarPositionEnum() {
    LEFT,
    RIGHT
}

/**
 * 雷达仪表
 *
 * @param modifier
 * @param content 仪表文本
 * @param sectorDistances 雷达扇区距离
 * @param backgroundColor 仪表背景色 默认黑
 * @param radarPosition 雷达显示位置 默认右
 * @param radarClickEnabled 雷达点击开关 默认关
 * @param onRadarClick 雷达点击事件
 */
@Composable
fun RadarInstrument(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    sectorDistances: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    backgroundColor: Color = Color.Black,
    radarPosition: RadarPositionEnum = RadarPositionEnum.RIGHT,
    radarClickEnabled: Boolean = false,
    onRadarClick: () -> Unit = {}
) {
    var radarSize by remember {
        mutableIntStateOf(0)
    }
    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
//                shape = CutCornerShape(topEndPercent = 50, bottomEndPercent = 50),
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (radarPosition == RadarPositionEnum.RIGHT) {
            content()
            Spacer(modifier = Modifier.weight(1f)) //占据空间
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .offset { IntOffset(radarSize / 2, 0) }
                .onSizeChanged { size ->
                    radarSize = size.width
                }
                .noEffectClickable(radarClickEnabled) {
                    onRadarClick()
                }
        ) {
            // 裁剪左半边并设置背景色
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f) // 只占左半边
                    .background(color = backgroundColor)
            )
            Radar(
                sectorDistances = sectorDistances,
                backgroundColor = backgroundColor
            )
        }
        if (radarPosition == RadarPositionEnum.LEFT) {
            Spacer(modifier = Modifier.weight(1f)) //占据空间
            content()
        }
    }
}

/**
 * 雷达文本数据
 *
 * @param modifier
 * @param title 标题
 * @param text 正文
 * @param unit 单位
 * @param color 文字颜色 默认白
 */
@Composable
fun RadarTextData(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    unit: String = "",
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleStyle: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = Color.White
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AutoScrollingText(
            text = text,
            color = color,
            style = textStyle,
            modifier = Modifier.fillMaxWidth()
        )
        AutoScrollingText(
            text = if (unit.isNotBlank()) "${title}(${unit})" else title,
            color = color,
            style = titleStyle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RadarInstrumentPreview() {
    ComposeTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RadarInstrument(modifier = Modifier
                .width(300.dp)
                .height(150.dp),
                radarPosition = RadarPositionEnum.RIGHT,
                content = {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadarTextData(
                            modifier = Modifier.weight(1f),
                            title = "参数1",
                            text = "aaa",
                        )
                        RadarTextData(
                            modifier = Modifier.weight(1f),
                            title = "参数2",
                            text = "bbbbbbbbbbbbb",
                        )
                    }
                }
            )
        }
    }
}