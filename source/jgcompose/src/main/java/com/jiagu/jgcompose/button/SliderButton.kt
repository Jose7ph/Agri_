package com.jiagu.jgcompose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.longPressListener
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors
import com.jiagutech.jgcompose.ui.slider.Slider

/**
 * 滑块开关切换按钮
 * 可通过滑动或长按按钮控制
 *
 * @param sliderTitle 滑块文本 可选
 * @param sliderWidth 滑块长度
 * @param height 组件高度
 * @param buttonName 按钮名称
 * @param buttonWidth 按钮宽度
 */
@Composable
fun SliderSwitchButton(
    sliderTitle: String = "",
    sliderWidth: Dp,
    height: Dp,
    buttonName: String,
    buttonWidth: Dp,
    onSuccess: (Boolean) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    var success by remember {
        mutableStateOf(false)
    }
    var longPressState by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = success) {
        onSuccess(success)
    }
    Row(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Slider(
            sliderWidth = sliderWidth,
            sliderHeight = height,
            sliderTitle = sliderTitle,
            longPressState = longPressState,
            longPressTime = 2f
        ) {
            success = it
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier
            .fillMaxHeight()
            .width(buttonWidth)
            .background(
                color = if (success) extendedColors.buttonDisabled else MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
            .then(if (success) {
                Modifier
            } else {
                Modifier.pointerInput(null) {
                    longPressListener(progress = { longPressState = true },
                        done = { longPressState = false })
                }
            }),
            contentAlignment = Alignment.Center) {
            AutoScrollingText(text = buttonName, color = Color.White)
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun SliderButtonPreview() {
    ComposeTheme {
        Column {
            SliderSwitchButton(
                sliderWidth = 300.dp, height = 30.dp, buttonName = "conform", buttonWidth = 60.dp
            ) {}
        }
    }
}