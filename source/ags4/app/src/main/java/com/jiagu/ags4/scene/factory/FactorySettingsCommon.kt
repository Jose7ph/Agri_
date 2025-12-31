package com.jiagu.ags4.scene.factory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.scene.work.settings.titleWeight
import com.jiagu.ags4.scene.work.settings.valueWeight
import com.jiagu.device.vkprotocol.VKAuthTool
import com.jiagu.jgcompose.button.GroupAskButton
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.counter.FloatChangeAskCounter
import com.jiagu.jgcompose.counter.SliderTitleChangeAskCounter
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 行分割box
 */
@Composable
fun RowSeparationBox(modifier: Modifier = Modifier, title: String) {
    val thickness = 1.dp
    val color = Color.Gray
    //横线样式
    val horizontalLine = @Composable { m: Modifier ->
        Column(
            modifier = m, verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(), thickness = thickness, color = color
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(), thickness = thickness, color = color
            )
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        horizontalLine(Modifier.weight(1f))
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(Dp.Infinity),
                color = color
            )
        }
        horizontalLine(Modifier.weight(1f))
    }
}

/**
 * Group button row
 *
 * @param title
 * @param titleTip
 * @param items
 * @param indexes
 * @param number
 * @param onClick
 * @receiver
 */
@Composable
fun GroupButtonRow(
    title: String,
    titleTip: String? = null,
    items: List<String>,
    indexes: List<Int>,
    number: Int,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TitleRowText(title = title, titleTip = titleTip)
        GroupButton(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(),
            items = items,
            indexes = indexes,
            number = number
        ) { idx, _ ->
            onClick(idx)
        }
    }
}

/**
 * Group button row
 *
 * @param title
 * @param titleTip
 * @param items
 * @param indexes
 * @param number
 * @receiver
 */
@Composable
fun GroupAskButtonRow(
    title: String,
    titleTip: String? = null,
    items: List<String>,
    indexes: List<Int>,
    number: Int,
    askPopup: @Composable (Int, String,(Boolean)->Unit) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TitleRowText(title = title, titleTip = titleTip)
        GroupAskButton(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(),
            items = items,
            indexes = indexes,
            number = number,
            askPopup = askPopup
        )
    }
}

/**
 * 计数器行
 *
 */
@Composable
fun FactoryXYCounterRow(
    title: Int,
    min: Float = 0f,
    max: Float = 100f,
    step: Float = 1f,
    number: Float = min,
    fraction: Int = 0,
    textColor: Color = Color.Gray,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit = {},
) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.weight(titleWeight)) {
            AutoScrollingText(
                text = stringResource(id = title),
                color = textColor,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleSmall
            )
        }
        FloatChangeAskCounter(modifier = Modifier.height(30.dp).weight(valueWeight),
            max = max,
            min = min,
            step = step,
            number = number,
            fraction = fraction,
            onConfirm = { onConfirm(it) },
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun FactorySliderAskCounter(
    title: String,
    number: Float,
    min: Float,
    max: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    SliderTitleChangeAskCounter(title = title,
        number = number,
        min = min,
        max = max,
        fraction = 0,
        textStyle = MaterialTheme.typography.bodySmall,
        titleColor = Color.Gray,
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss,
    )
}

/**
 * 提示文本
 *
 * @param text 文本内容
 */
@Composable
fun TipText(text: AnnotatedString) {
    Text(
        text = text, style = MaterialTheme.typography.bodySmall.copy(
            lineHeight = 16.sp
        ), color = Color.Red
    )
}

/**
 * title行文本
 *
 */
@Composable
fun TitleRowText(
    title: String,
    titleTip: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color.Gray,
            style = MaterialTheme.typography.titleSmall
        )
        titleTip?.let {
            AutoScrollingText(
                text = it,
                modifier = Modifier.width(Dp.Infinity),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleSmall,
                color = Color.Red
            )
        }
    }
}