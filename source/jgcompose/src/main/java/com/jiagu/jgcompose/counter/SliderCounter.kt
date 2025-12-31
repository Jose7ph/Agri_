package com.jiagu.jgcompose.counter

import android.R.attr.textColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors
import com.jiagu.jgcompose.utils.toString

/**
 * 带title的Slider counter
 *
 * @param title 标题
 * @param number 默认值
 * @param fraction 小数位数
 * @param min 最小值
 * @param max 最大值
 * @param height counter高度
 * @param step 步幅
 * @param textStyle 文本样式
 * @param sliderColors 滑块颜色
 * @param converter 格式化
 * @param onConfirm 确认回调 return最终值
 */
@Composable
fun SliderTitleCounter(
    title: String,
    number: Float,
    fraction: Int,
    min: Float,
    max: Float,
    height: Dp = 30.dp,
    step: Float = 1f,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    titleColor: Color = Color.Black,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    titleSpace: Dp = 10.dp,
    numberPrefix: String = "",
    numberPrefix2: String = "",
    enabled: Boolean = true,
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = LocalExtendedColors.current.buttonDisabled,
    ),
    converter: ConverterPair? = null,
    onConfirm: (Float) -> Unit,
) {
    //当前值
    var num by remember {
        mutableFloatStateOf(number)
    }
    LaunchedEffect(key1 = number) {
        if (num != number) {
            num = number
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(titleSpace)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), // 确保 Row 占据全宽
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                //标题
                AutoScrollingText(
                    text = title,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(), // 在 Box 内可以使用 fillMaxWidth
                    color = titleColor,
                    style = textStyle
                )
            }
            Box(
                modifier = Modifier.padding(start = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                val t = converter?.first?.invoke(num) ?: num.toString(fraction)
                //counter值
                AutoScrollingText(
                    text = if (num > 0) "$numberPrefix$numberPrefix2$t" else "$numberPrefix$t",
                    color = valueColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier, // 移除 fillMaxWidth()，让它使用自然宽度
                    style = textStyle
                )
            }
        }
        SliderCounter(
            step = step,
            number = number,
            valueRange = min..max,
            modifier = Modifier.height(height),
            sliderColors = sliderColors,
            enabled=enabled,
            onValueChange = {
                num = it
            },
            onValueChangeFinished = {
                num = it
                onConfirm(it)
            })
    }
}

/**
 * 带title的Slider counter
 *
 * @param title 标题
 * @param number 默认值
 * @param fraction 小数位数
 * @param min 最小值
 * @param max 最大值
 * @param height counter高度
 * @param step 步幅
 * @param textStyle 文本样式
 * @param textColor 文本颜色
 * @param sliderColors 滑块颜色
 * @param converter 格式化
 * @param onConfirm 确认回调 return最终值
 * @param onDismiss 取消回调
 */
@Composable
fun SliderTitleChangeAskCounter(
    title: String,
    number: Float,
    fraction: Int,
    min: Float,
    max: Float,
    height: Dp = 30.dp,
    step: Float = 1f,
    enable: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    titleColor: Color = Color.Black,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = LocalExtendedColors.current.buttonDisabled,
        disabledInactiveTickColor = Color.Transparent,
        disabledActiveTickColor = Color.Transparent,
    ),
    converter: ConverterPair? = null,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    //当前值
    var num by remember {
        mutableFloatStateOf(number)
    }
    LaunchedEffect(key1 = number) {
        if (num != number) {
            num = number
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier, contentAlignment = Alignment.Center
            ) {
                //标题
                AutoScrollingText(
                    text = title,
                    textAlign = TextAlign.Start,
                    modifier = Modifier,
                    color = titleColor,
                    style = textStyle
                )
            }
            Box(
                modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
            ) {
                //counter值
                AutoScrollingText(
                    text = converter?.first?.invoke(num) ?: num.toString(fraction),
                    color = valueColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    style = textStyle
                )
            }
        }
        SliderChangeAskCounter(
            step = step,
            enable = enable,
            number = number,
            valueRange = min..max,
            modifier = Modifier.height(height),
            sliderColors = sliderColors,
            onValueChange = {
                num = it
            },
            onDismiss = onDismiss,
            onValueChangeFinished = {
                num = it
                onConfirm(it)
            })
    }
}

/**
 * Slider counter
 *
 * @param modifier 装饰器
 * @param number 默认值
 * @param step 步幅
 * @param valueRange 值范围
 * @param steps 节点数量 默认 ((max-min)/step) - 1
 * @param sliderColors 滑块颜色
 * @param onValueChange 值变化回调
 * @param onValueChangeFinished 值最终变化回调
 */
@Composable
fun SliderCounter(
    modifier: Modifier = Modifier,
    number: Float,
    step: Float = 1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = ((valueRange.endInclusive - valueRange.start) / step).toInt() - 1,
    enabled: Boolean = true,
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = LocalExtendedColors.current.buttonDisabled,
    ),
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
) {
    //当前值
    var num by remember {
        mutableFloatStateOf(number)
    }
    //按钮状态
    val minusEnable = num > valueRange.start && enabled
    val plusEnable = num < valueRange.endInclusive && enabled

    //防止父组件与组件内部值不一致问题
    LaunchedEffect(key1 = number) {
        if (num != number) {
            num = number
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CounterButton(image = R.drawable.minus, enabled = minusEnable) {
            num -= step
            onValueChange(num)
            onValueChangeFinished(num)
        }
        Slider(
            value = num,
            onValueChange = {
                num = it
                onValueChange(num)
            },
            onValueChangeFinished = {
                onValueChangeFinished(num)
            },
            enabled = enabled,
            colors = sliderColors,
            steps = steps,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
        )
        CounterButton(image = R.drawable.plus, enabled = plusEnable) {
            num += step
            onValueChange(num)
            onValueChangeFinished(num)
        }
    }
}

/**
 * 值变更提示Slider counter
 *
 * @param modifier 装饰器
 * @param number 默认值
 * @param step 步幅
 * @param valueRange 值范围
 * @param steps 节点数量 默认 ((max-min)/step) - 1
 * @param sliderColors 滑块颜色
 * @param onDismiss 取消回调
 * @param onValueChange 值变化回调
 * @param onValueChangeFinished 值最终变化回调
 */
@Composable
fun SliderChangeAskCounter(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    number: Float,
    step: Float = 1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = ((valueRange.endInclusive - valueRange.start) / step).toInt() - 1,
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTickColor = Color.Transparent,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTickColor = Color.Transparent,
        inactiveTrackColor = LocalExtendedColors.current.buttonDisabled,
        disabledInactiveTickColor = Color.Transparent,
        disabledActiveTickColor = Color.Transparent,
    ),
    onDismiss: () -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
) {
    val context = LocalContext.current
    //原值 用于还原数据用
    var oldValue by remember {
        mutableFloatStateOf(number)
    }
    //当前值
    var num by remember {
        mutableFloatStateOf(number)
    }
    //按钮状态
    val minusEnable = num > valueRange.start && enable
    val plusEnable = num < valueRange.endInclusive && enable

    //值修改弹窗 num：最新值
    val showAsk = { current: Float ->
        context.showDialog {
            PromptPopup(content = stringResource(id = R.string.confirm_change), onConfirm = {
                oldValue = current
                onValueChangeFinished(current)
                context.hideDialog()
            }, onDismiss = {
                num = oldValue
                onValueChangeFinished(oldValue)
                onDismiss()
                context.hideDialog()
            })
        }
    }

    //防止父组件与组件内部值不一致问题
    LaunchedEffect(key1 = number) {
        if (num != number) {
            oldValue = number
            num = number
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CounterButton(image = R.drawable.minus, enabled = minusEnable) {
            num -= step
            onValueChange(num)
            showAsk(num)
        }
        Slider(
            enabled = enable,
            value = num,
            onValueChange = {
                num = it
                onValueChange(num)
            },
            onValueChangeFinished = {
                showAsk(num)
            },
            colors = sliderColors,
            steps = steps,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
        )
        CounterButton(image = R.drawable.plus, enabled = plusEnable) {
            num += step
            onValueChange(num)
            showAsk(num)
        }
    }
}

/**
 * Counter button
 * @param image 按钮图片
 * @param enabled 按钮状态
 * @param onClick 点击事件
 */
@Composable
private fun CounterButton(image: Int, enabled: Boolean = true, onClick: () -> Unit) {
    val extendedColors = LocalExtendedColors.current
    val buttonColor =
        if (enabled) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled
    Box(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .background(
                color = buttonColor, shape = MaterialTheme.shapes.extraSmall
            )
            .clickable(enabled = enabled) {
                onClick()
            }
    ) {
        Image(
            painter = painterResource(id = image), contentDescription = "symbol"
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun SliderCounterPreview() {
    ComposeTheme {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            var num by remember {
                mutableStateOf(0f)
            }
            SliderChangeAskCounter(
                modifier = Modifier.height(30.dp),
                enable = false,
                number = num,
                valueRange = 0f..100f,
                onValueChange = {
                    num = it
                },
                onValueChangeFinished = {
                    num = it
                },
                onDismiss = {}
            )
            HorizontalDivider()
            SliderTitleCounter(
                title = "title",
                min = 10f,
                max = 100f,
                number = 0f,
                fraction = 0,
                onConfirm = {

                })
        }
    }
}