package com.jiagu.jgcompose.counter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.longPressListener
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors
import com.jiagu.jgcompose.utils.toString

@Composable
fun RangeSliderCounter(
    modifier: Modifier,
    value: ClosedFloatingPointRange<Float>,
    step: Float = 1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = ((valueRange.endInclusive - valueRange.start) / step).toInt() - 1,
    titleContent: @Composable (ClosedFloatingPointRange<Float>) -> Unit = {},
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = LocalExtendedColors.current.buttonDisabled,
    ),
    onChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    var start by remember { mutableFloatStateOf(value.start) }
    var end by remember { mutableFloatStateOf(value.endInclusive) }
    LaunchedEffect(value) {
        if (start != value.start) {
            start = value.start
        }
        if (end != value.endInclusive) {
            end = value.endInclusive
        }
    }

    var confirmEnable = start < end && start != -1f && end != -1f
    val minusEnable = start > valueRange.start && confirmEnable
    val plusEnable = end < valueRange.endInclusive && confirmEnable

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        titleContent(start..end)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CounterButton(
                image = R.drawable.minus,
                enabled = minusEnable,
            ) {
                start -= step
                if (start < valueRange.start) start = valueRange.start
                if (start > valueRange.endInclusive) start = valueRange.endInclusive
                onChange(start..end)
            }
            RangeSlider(
                value = start..end,
                onValueChange = {
                    start = it.start
                    end = it.endInclusive
                    onChange(start..end)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                valueRange = valueRange,
                steps = steps,
                colors = sliderColors,
                onValueChangeFinished = {
                    onChange(start..end)
                }
            )
            CounterButton(
                image = R.drawable.plus,
                enabled = plusEnable,
            ) {
                end += step
                if (end > valueRange.endInclusive) end = valueRange.endInclusive
                if (end < valueRange.start) end = valueRange.start
                onChange(start..end)
            }
        }
    }
}

@Composable
fun RangeSliderInputCounter(
    modifier: Modifier,
    value: ClosedFloatingPointRange<Float>,
    step: Float = 1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    fraction: Int = 0,
    steps: Int = ((valueRange.endInclusive - valueRange.start) / step).toInt() - 1,
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = LocalExtendedColors.current.buttonDisabled,
    ),
    onChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    var start by remember { mutableFloatStateOf(value.start) }
    var end by remember { mutableFloatStateOf(value.endInclusive) }
    var startText by remember { mutableStateOf(start.toString(fraction)) }
    var endText by remember { mutableStateOf(end.toString(fraction)) }
    LaunchedEffect(value) {
        if (start != value.start) {
            start = value.start
            startText = start.toString(fraction)
        }
        if (end != value.endInclusive) {
            end = value.endInclusive
            endText = end.toString(fraction)
        }
    }

    var confirmEnable = start < end && start != -1f && end != -1f
    val minusEnable = start > valueRange.start && confirmEnable
    val plusEnable = end < valueRange.endInclusive && confirmEnable

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                NormalTextField(
                    text = startText,
                    onValueChange = {
                        if (it.isBlank()) {
                            startText = ""
                            start = -1f
                            onChange(start..end)
                            return@NormalTextField
                        }
                        start = it.toFloat()
                        startText = start.toString(0)
                        onChange(start..end)
                    },
                    showClearIcon = false,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    borderColor = if (confirmEnable) MaterialTheme.colorScheme.outline else Color.Red
                )
            }
            Text(modifier = Modifier, text = "~")
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                NormalTextField(
                    text = endText,
                    onValueChange = {
                        if (it.isBlank()) {
                            endText = ""
                            end = -1f
                            onChange(start..end)
                            return@NormalTextField
                        }
                        end = it.toFloat()
                        endText = end.toString(0)
                        onChange(start..end)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    showClearIcon = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    borderColor = if (confirmEnable) MaterialTheme.colorScheme.outline else Color.Red
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CounterButton(
                image = R.drawable.minus,
                enabled = minusEnable
            ) {
                start -= step
                if (start < valueRange.start) start = valueRange.start
                if (start > valueRange.endInclusive) start = valueRange.endInclusive
                startText = start.toString(0)
                onChange(start..end)
            }
            RangeSlider(
                value = start..end,
                onValueChange = {
                    start = it.start
                    end = it.endInclusive
                    startText = start.toString(0)
                    endText = end.toString(0)
                    onChange(start..end)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                valueRange = valueRange,
                steps = steps,
                colors = sliderColors,
                onValueChangeFinished = {
                    onChange(start..end)
                }
            )
            CounterButton(
                image = R.drawable.plus,
                enabled = plusEnable
            ) {
                end += step
                if (end > valueRange.endInclusive) end = valueRange.endInclusive
                if (end < valueRange.start) end = valueRange.start
                endText = end.toString(0)
                onChange(start..end)
            }
        }
    }
}


/**
 * Counter button
 *
 * @param image 按钮图片
 * @param enabled 按钮状态
 * @param onClick 点击事件
 */
@Composable
private fun CounterButton(
    image: Int,
    enabled: Boolean = true,
    onClick: (ClickStatusEnum) -> Unit,
) {
    val extendedColors = LocalExtendedColors.current
    val buttonColor =
        if (enabled) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled
    Box(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .fillMaxHeight()
            .background(
                color = buttonColor, shape = MaterialTheme.shapes.extraSmall
            )
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        longPressListener(progress = {
                            onClick(ClickStatusEnum.TAP_START)
                        }, done = {
                            onClick(ClickStatusEnum.TAP_END)
                        })
                    }
                } else {
                    Modifier
                })) {
        Image(
            painter = painterResource(id = image), contentDescription = "symbol"
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RangeSliderCounterPreview() {
    ComposeTheme {
        Column {
            RangeSliderCounter(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                value = 30.0009f..50.11119f,
                valueRange = 0f..100f,
                step = 1f,
                titleContent = {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${it.start.toString(0)} ~ ${it.endInclusive.toString(0)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                onChange = {}
            )
            RangeSliderInputCounter(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                value = 30.0009f..50.11119f,
                valueRange = 0f..100f,
                step = 1f,
                onChange = {}
            )
        }
    }
}