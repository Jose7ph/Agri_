package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.ui.theme.buttonDisabled
import com.jiagu.jgcompose.counter.RangeSliderInputCounter
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.utils.ImeVisible

fun checkConfirmEnable(v: ClosedFloatingPointRange<Float>): Boolean {
    return v.start < v.endInclusive && v.start != -1f && v.endInclusive != -1f
}

@Composable
fun EditNaviLinePanel(
    modifier: Modifier,
    step: Float = 1f,
    value: ClosedFloatingPointRange<Float> = 0f..100f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    onChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onConfirm: (ClosedFloatingPointRange<Float>) -> Unit = {},
    onCancel: () -> Unit = {},
) {
    var v by remember { mutableStateOf<ClosedFloatingPointRange<Float>>(value) }
    LaunchedEffect(value) { }
    if (v != value) {
        v = value
    }
    var confirmEnable = checkConfirmEnable(v)
    Column(
        modifier = modifier
            .background(color = Color.White, shape = MaterialTheme.shapes.medium)
            .padding(top = 10.dp)
            .clip(shape = MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = stringResource(R.string.delete_line),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        RangeSliderInputCounter(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(80.dp),
            value = value,
            valueRange = valueRange,
            step = step
        ) {
            v = it
            confirmEnable = checkConfirmEnable(it)
            onChange(it)
        }
        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .noEffectClickable {
                        onCancel()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.cancel), color = Color.White)
            }
            VerticalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(),
                color = Color.Gray
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (confirmEnable) MaterialTheme.colorScheme.primary else buttonDisabled
                    )
                    .noEffectClickable(confirmEnable) {
                        onConfirm(v)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.confirm), color = Color.White)
            }
        }
    }
}

@Composable
fun EditNaviLinePanel(
    modifier: Modifier,
    start: Int,
    end: Int = start,
    max: Int,
    onConfirm: () -> Unit,
    onChange: (Int, Int) -> Unit,
    onCancel: () -> Unit = {},
) {
    //软键盘状态
    var imeState by remember {
        mutableStateOf(false)
    }

    var startText by remember { mutableStateOf("") }
    var endText by remember { mutableStateOf("") }
    LaunchedEffect(start, end) {
        if (start.toString() != startText) {
            startText = if (start > 0) {
                start.toString()
            } else {
                ""
            }
        }
        if (end.toString() != endText) {
            endText = if (end > 0) {
                end.toString()
            } else {
                ""
            }
        }
    }
    ImeVisible { isVisible ->
        if (imeState == isVisible) {
            return@ImeVisible
        }
        imeState = isVisible
        //隐藏
        if (!imeState) {
            onChange(
                if (startText.isBlank()) -1 else startText.toInt(),
                if (endText.isBlank()) -1 else endText.toInt()
            )
        }
    }
    var confirmEnable =
        startText.isNotBlank() && endText.isNotBlank() && startText.toInt() < endText.toInt()
    Column(
        modifier = modifier
            .background(color = Color.White, shape = MaterialTheme.shapes.medium)
            .padding(top = 10.dp)
            .clip(shape = MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = stringResource(R.string.delete_line),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Row(
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 10.dp),
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
                        if (it.isBlank()) { //输入为空 且没有触发ime 则说明点击了删除图标 直接触发回调，其他情况通过ime显示 -> 隐藏来触发输入回调
                            startText = ""
                            if(!imeState){
                                onChange(-1, if (endText.isBlank()) -1 else endText.toInt())
                            }
                        } else {
                            startText = if (it.toInt() > max) {
                                max.toString()
                            } else {
                                it
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    borderColor = if (confirmEnable) MaterialTheme.colorScheme.outline else Color.Red
                )
            }
            Image(
                painter = painterResource(id = R.drawable.default_exchange),
                contentDescription = "exchange",
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .noEffectClickable {
                        startText = endText.also { endText = startText }
                        onChange(
                            if (startText.isBlank()) -1 else startText.toInt(),
                            if (endText.isBlank()) -1 else endText.toInt()
                        )
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                NormalTextField(
                    text = endText,
                    onValueChange = {
                        if (it.isBlank()) {//输入为空 且没有触发ime 则说明点击了删除图标 直接触发回调，其他情况通过ime显示 -> 隐藏来触发输入回调
                            endText = ""
                            if(!imeState){
                                onChange(
                                    if (startText.isBlank()) -1 else startText.toInt(),
                                    -1
                                )
                            }
                        } else {
                            endText = if (it.toInt() > max) {
                                max.toString()
                            } else {
                                it
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    borderColor = if (confirmEnable) MaterialTheme.colorScheme.outline else Color.Red
                )
            }
        }
        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .noEffectClickable {
                        onCancel()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.cancel), color = Color.White)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (confirmEnable && !imeState) MaterialTheme.colorScheme.primary else buttonDisabled
                    )
                    .noEffectClickable(confirmEnable && !imeState) { //软键盘弹出时不允许点击确定
                        onConfirm()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.confirm), color = Color.White)
            }
        }
    }
}

@Preview
@Composable
private fun EditNaviLinePanelPreview() {
    ComposeTheme {
        Column {
            EditNaviLinePanel(
                modifier = Modifier
                    .fillMaxWidth(),
                value = 30.0009f..50.11119f,
                valueRange = 0f..100f,
                step = 1f,
                onChange = {},
                onConfirm = {},
                onCancel = {}
            )
        }
    }
}