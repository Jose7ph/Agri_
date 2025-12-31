package com.jiagu.ags4.scene.work.settings

import BreathingLight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.utils.ParamTool
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.button.GroupButtons
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.ConverterPair

/**
 * 设置菜单通用的row生成
 */

//全局行高度
val settingsGlobalRowHeight = 36.dp

//全局行间距
val settingsGlobalColumnSpacer = 10.dp

//全局行间距
val settingsGlobalPaddingHorizontal = 10.dp

//全局按钮高度
val settingsGlobalButtonHeight = 30.dp

//全局title宽度
val settingsGlobalTitleWidth = 120.dp

//全局button組件 最大宽度
val settingsGlobalButtonComponentWidth = 200.dp

val switchButtonWidth = 60.dp


//全局行文本
@Composable
fun SettingsGlobalRowText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    AutoScrollingText(
        text = text,
        width = Dp.Infinity,
        modifier = modifier,
        style = style,
        textAlign = textAlign,
        color = textColor
    )
}

val titleWeight = 1f
val valueWeight = 1.5f

/**
 * 计数器行
 *
 */
@Composable
fun CounterRow(
    modifier: Modifier = Modifier,
    title: Int? = null,
    titleString: String? = null,
    counterType: String,
    intMin: Int = 0,
    intMax: Int = 100,
    intStep: Int = 1,
    intDefaultNumber: Int = intMin,
    floatMin: Float = 0f,
    floatMax: Float = 100f,
    floatStep: Float = 1f,
    floatDecimal: Int = 1,
    floatDefaultNumber: Float = floatMin,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    showTitle: Boolean = true,
    converter: ConverterPair? = null,
    onConfirm: (v: Float) -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(30.dp)
            .padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showTitle) {
            Box(modifier = Modifier.weight(titleWeight)) {
                val text = titleString ?: stringResource(id = title ?: 0)
                AutoScrollingText(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    style = style,
                    color = textColor,
                    textAlign = TextAlign.Start,
                )
            }
        }
        if (counterType == COUNTER_TYPE_INT) {
            FloatCounter(
                modifier = Modifier
                    .weight(valueWeight)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.extraSmall
                    ),
                number = intDefaultNumber.toFloat(),
                min = intMin.toFloat(),
                max = intMax.toFloat(),
                step = intStep.toFloat(),
                fraction = 0,
                converterPair = converter,
            ) {
                onConfirm(it.toFloat())
            }
        } else if (counterType == COUNTER_TYPE_FLOAT) {
            FloatCounter(
                modifier = Modifier
                    .weight(valueWeight)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.extraSmall
                    ),
                number = floatDefaultNumber,
                min = floatMin,
                max = floatMax,
                step = floatStep,
                fraction = floatDecimal,
                converterPair = converter,
            ) {
                onConfirm(it)
            }
        }
    }
}

/**
 * 单选按钮行
 */
@Composable
fun GroupButtonRow(
    modifier: Modifier = Modifier,
    title: Int,
    defaultNumber: Int = 0,
    names: List<String>,
    values: List<Int>,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    currentNumber: Int? = null,
    onClick: (value: Int) -> Unit,
) {
    var number by remember { mutableIntStateOf(defaultNumber) }
    if (currentNumber != null) {
        number = currentNumber
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(settingsGlobalRowHeight)
            .padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(titleWeight)) {
            AutoScrollingText(
                modifier = Modifier,
                text = stringResource(title),
                style = style,
                textAlign = TextAlign.Start,
                color = textColor
            )
        }
        GroupButton(
            modifier = modifier
                .weight(valueWeight)
                .height(settingsGlobalButtonHeight),
            items = names,
            indexes = values,
            number = number,
        ) { id, _ ->
            number = id
            onClick(id)
        }
    }
}

/**
 * 多选按钮行
 */
@Composable
fun GroupButtonsRow(
    modifier: Modifier = Modifier,
    title: Int,
    defaultNumber: Int = 0,
    names: List<String>,
    values: List<Int>,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    onClick: (value: Int) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(settingsGlobalRowHeight)
            .padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(titleWeight)) {
            SettingsGlobalRowText(text = stringResource(id = title), style = style)
        }
        GroupButtons(
            modifier = modifier
                .weight(valueWeight)
                .width(settingsGlobalButtonComponentWidth)
                .height(settingsGlobalButtonHeight),
            items = names,
            indexes = values,
            numbers = ParamTool.BitIntToArrayIndexes(defaultNumber),
        ) {
            onClick(ParamTool.ArrayIndexesToBitInt(it))
        }
    }
}

/**
 * 开关按钮行
 * Switch最小高度导致样式有问题
 */
@Composable
fun SwitchButtonRow(
    modifier: Modifier = Modifier,
    title: Int,
    defaultChecked: Boolean = false,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    explain: String = "",
    explainColor: Color = Color.Black,
    showLight: Boolean = false,
    lightCurrentInt: Int = 0,
    refreshKey: Int = 0,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(settingsGlobalRowHeight)
            .padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.width(settingsGlobalTitleWidth)) {
            SettingsGlobalRowText(text = stringResource(id = title), style = style)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = explain,
                maxLines = 2,
                style = MaterialTheme.typography.labelMedium,
                color = explainColor,
                textAlign = TextAlign.Start,
            )
        }
        if (showLight) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .padding(end = 10.dp, top = 6.dp, bottom = 6.dp)
            ) {
                BreathingLight(modifier = Modifier.size(40.dp), lightCurrentInt, refreshKey)
            }
        }
        Box(
            modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterEnd
        ) {
            SwitchButton(
                defaultChecked = defaultChecked,
                height = settingsGlobalButtonHeight,
                width = switchButtonWidth
            ) {
                onCheckedChange(it)
            }
        }
    }
}

/**
 * 单个按钮行
 *
 */
@Composable
fun SingleButtonRow(
    modifier: Modifier = Modifier,
    title: Int,
    buttonText: Int,
    onClick: () -> Unit,
    style: TextStyle = MaterialTheme.typography.labelLarge,
) {
    Row(
        modifier = modifier
            .height(settingsGlobalRowHeight)
            .padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(titleWeight)) {
            SettingsGlobalRowText(text = stringResource(id = title), style = style)
        }
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .weight(valueWeight)
                .height(settingsGlobalButtonHeight),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            AutoScrollingText(
                text = stringResource(id = buttonText),
                style = style,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * 普通文本框行
 */
@Composable
fun TextFieldRow(
    modifier: Modifier = Modifier,
    title: Int,
    style: TextStyle,
    defaultValue: String,
    onValueChange: (String) -> Unit = {},
) {
    var text by remember { mutableStateOf(defaultValue) }
    Row(
        modifier = modifier.padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.width(settingsGlobalTitleWidth)) {
            SettingsGlobalRowText(text = stringResource(id = title), style = style)
        }
        NormalTextField(
            text = text,
            modifier = Modifier
                .weight(1f)
                .height(25.dp)
                .wrapContentHeight()
                .background(
                    color = Color.White, shape = MaterialTheme.shapes.small
                ),
            onValueChange = {
                text = it
                onValueChange(it)
            },
            borderColor = Color.Black,
            showClearIcon = false,
            textStyle = MaterialTheme.typography.labelLarge
        )
    }
}

//全局行文本
@Composable
fun RtkRowText(
    modifier: Modifier = Modifier,
    text: String,
    explain: String,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Row {
        AutoScrollingText(
            text = text,
            width = Dp.Infinity,
            modifier = modifier,
            style = style,
            textAlign = textAlign,
            color = textColor
        )
        AutoScrollingText(
            text = explain,
            width = Dp.Infinity,
            modifier = modifier,
            style = style,
            textAlign = textAlign,
            color = textColor
        )
    }
}

/**
 * 跳转按钮行
 *
 */
@Composable
fun JumpButtonRow(
    modifier: Modifier = Modifier,
    title: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(settingsGlobalRowHeight)
            .padding(horizontal = settingsGlobalPaddingHorizontal)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.width(settingsGlobalTitleWidth)) {
            SettingsGlobalRowText(
                text = stringResource(id = title), style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .width(settingsGlobalButtonComponentWidth),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = onClick, modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun FrameColumn(
    borderColor: Color = MaterialTheme.colorScheme.onPrimary,
    showVerticalPadding: Boolean = true,
    backgroundColor: Color = Color.Unspecified,
    content: @Composable () -> Unit,
) {
    val paddingValue = 4.dp
    Column(
        modifier = Modifier
            .padding(horizontal = paddingValue)
            .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = borderColor
            ),
        verticalArrangement = Arrangement.Center
    ) {
        if (showVerticalPadding) {
            Spacer(modifier = Modifier.height(paddingValue))
        }
        content()
        if (showVerticalPadding) {
            Spacer(modifier = Modifier.height(paddingValue))
        }
    }
}