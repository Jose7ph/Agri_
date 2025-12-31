package com.jiagu.jgcompose.card

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.counter.ConverterPair
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.counter.SliderCounter
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.utils.toString


/**
 * Card frame
 *
 * @param modifier
 * @param title 卡片标题
 * @param titleRightContent 卡片标题右侧内容
 * @param content 卡片下方正文内容
 * @receiver
 */
@Composable
fun CardFrame(
    modifier: Modifier = Modifier,
    title: String,
    titleRightContent: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.small,
            )
            .clip(shape = MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AutoScrollingText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            titleRightContent()
        }
        content()
    }
}

/**
 * Card frame content row
 *
 * @param title
 * @param text
 * @param scales
 */
@Composable
fun CardFrameContentRow(
    title: String,
    text: String,
    scales: FloatArray = floatArrayOf(1f, 1f),
) {
    if (scales.size != 2) {
        return
    }
    CardFrameCustomRow {
        Box(modifier = Modifier.weight(scales[0])) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(scales[1])) {
            AutoScrollingText(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Card frame switch button
 *
 * @param title
 * @param defaultChecked
 * @param buttonWidth
 * @param buttonHeight
 * @param onChange
 * @receiver
 */
@Composable
fun CardFrameSwitchButtonRow(
    title: String,
    defaultChecked: Boolean,
    buttonWidth: Dp = 50.dp,
    buttonHeight: Dp = buttonWidth / 2,
    backgroundColors: List<Color> = listOf(
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primary
    ),
    onChange: (Boolean) -> Unit,
) {
    CardFrameCustomRow {
        AutoScrollingText(
            text = title,
            modifier = Modifier,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
        SwitchButton(
            backgroundColors = backgroundColors,
            width = buttonWidth, height = buttonHeight, defaultChecked = defaultChecked
        ) {
            onChange(it)
        }
    }
}

/**
 * Card frame title button
 *
 * @param title
 * @param text
 * @param enable
 * @param onClick
 * @receiver
 */
@Composable
fun CardFrameTitleButtonRow(
    title: String,
    text: String,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    CardFrameCustomRow {
        AutoScrollingText(
            text = title,
            modifier = Modifier,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
        CardFrameButton(text = text, enable = enable, onClick = onClick)
    }
}

/**
 * Card frame title counter
 *
 * @param title
 * @param number
 * @param min
 * @param max
 * @param step
 * @param fraction
 * @param enabled
 * @param converterPair
 * @param onValueChange
 * @receiver
 */
@Composable
fun CardFrameTitleCounterRow(
    title: String,
    number: Float,
    min: Float,
    max: Float,
    step: Float,
    fraction: Int,
    enabled: Boolean = true,
    converterPair: ConverterPair? = null,
    scales: FloatArray = floatArrayOf(1f, 1f),
    onValueChange: (Float) -> Unit,
) {
    if (scales.size != 2) {
        return
    }
    CardFrameCustomRow {
        Box(modifier = Modifier.weight(scales[0])) {
            AutoScrollingText(
                text = title,
                modifier = Modifier,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
        FloatCounter(
            modifier = Modifier
                .weight(scales[1])
                .height(30.dp),
            number = number,
            min = min,
            max = max,
            step = step,
            fraction = fraction,
            enabled = enabled,
            converterPair = converterPair,
            onValueChange = onValueChange
        )
    }
}

/**
 * Card frame title slider counter row
 *
 * @param title
 * @param number
 * @param valueRange
 * @param step
 * @param scales
 * @param fraction
 * @param onValueChange
 * @param onValueChangeFinished
 * @receiver
 * @receiver
 */
@Composable
fun CardFrameTitleSliderCounterRow(
    title: String,
    number: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    step: Float,
    scales: FloatArray = floatArrayOf(1f, 1f),
    fraction: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
) {
    if (scales.size != 2) {
        return
    }
    var currentNumber by remember { mutableFloatStateOf(number) }
    CardFrameCustomRow {
        Row(
            modifier = Modifier
                .weight(scales[0])
                .fillMaxWidth()
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoScrollingText(
                text = title,
                modifier = Modifier,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Text(
                text = currentNumber.toString(fraction),
            )
        }
        SliderCounter(
            modifier = Modifier
                .weight(scales[1])
                .height(30.dp),
            number = number,
            step = step,
            valueRange = valueRange,
            onValueChange = {
                currentNumber = it
                onValueChange(it)
            },
            onValueChangeFinished = {
                currentNumber = it
                onValueChangeFinished(it)
            }
        )
    }
}

/**
 * Card frame custom row
 *
 * @param content
 * @receiver
 */
@Composable
fun CardFrameCustomRow(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }
}

@Composable
fun CardFrameButton(
    text: String,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .width(100.dp)
            .height(30.dp),
        enabled = enable
    ) {
        AutoScrollingText(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun CardFramePreview() {
    ComposeTheme {
        Column {
            CardFrame(modifier = Modifier.width(300.dp), title = "card title", titleRightContent = {
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {},
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("aaaaaaa")
                }
            }, content = {
                CardFrameContentRow(title = "aaaaaaa", text = "bbbbbbbbbbbb")
                CardFrameSwitchButtonRow(
                    title = "bbbb", defaultChecked = true
                ) {}
                CardFrameTitleButtonRow(title = "bb", text = "aa", onClick = {})
                CardFrameButton(text = "aa", onClick = {})
                CardFrameTitleSliderCounterRow(
                    title = "slider:",
                    number = 1f,
                    step = 1f,
                    fraction = 0,
                    onValueChange = {},
                    onValueChangeFinished = {}
                )
                CardFrameTitleCounterRow(
                    title = "aa",
                    min = 1f,
                    max = 10f,
                    number = 1f,
                    fraction = 1,
                    step = 1f,
                    onValueChange = {})
            })
        }
    }
}