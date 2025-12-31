package com.jiagu.jgcompose.label

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.picker.DateRangePicker
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 日期范围选择 label
 *
 * @param modifier 装饰器
 * @param labelName label名称
 * @param labelWidth label宽度
 * @param defaultStartDate 默认开始时间 当defaultStartDate或defaultEndDate任意一个为空时，显示请选择
 * @param defaultEndDate 默认结束时间 当defaultStartDate或defaultEndDate任意一个为空时，显示请选择
 * @param showTime 是否显示时分秒 默认false 时分秒只显示00:00:00 ~ 23:59:59
 * @param onConfirm 确定回调
 * p1:日期范围[yyyy-MM-dd ~ yyyy-MM-dd],
 * p2:开始日期[yyyy-MM-dd],
 * p3:开始时间[yyyy-MM-dd HH:mm:ss],
 * p4:结束日期[yyyy-MM-dd],
 * p5:结束时间[yyyy-MM-dd HH:mm:ss]
 * @param onCancel 取消回调
 */
@Composable
fun DateRangeLabel(
    modifier: Modifier = Modifier,
    labelName: String,
    labelWidth: Dp,
    defaultStartDate: String = "",
    defaultEndDate: String = "",
    showTime: Boolean = false,
    onConfirm: (String, String, String, String, String) -> Unit,
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var text by remember {
        mutableStateOf(if (defaultStartDate.isEmpty() || defaultEndDate.isEmpty()) "" else ("$defaultStartDate ~ $defaultEndDate"))
    }
    Label(
        modifier = modifier, labelWidth = labelWidth, labelName = labelName, content = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .noEffectClickable {
                        context.showDialog {
                            DateRangePicker(
                                defaultStartDate = defaultStartDate,
                                defaultEndDate = defaultEndDate,
                                onConfirm = { rang, sd, st, ed, et ->
                                    text = if (showTime) {
                                        "$st ~ $et"
                                    } else {
                                        rang
                                    }
                                    onConfirm(rang, sd, st, ed, et)
                                    context.hideDialog()
                                },
                                onCancel = {
                                    text = ""
                                    onCancel()
                                    context.hideDialog()
                                })
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (text.isEmpty()) {
                        AutoScrollingText(
                            text = stringResource(R.string.please_select),
                            color = Color.Gray,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        })
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DateRangeLabelPreview() {
    ComposeTheme {
        Column {
            DateRangeLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelWidth = 60.dp,
                labelName = "dateRange",
                onConfirm = { _, _, _, _, _ -> }
            )
        }
    }
}