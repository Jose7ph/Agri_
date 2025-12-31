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
import com.jiagu.jgcompose.picker.DatePicker
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 日期范围选择 label
 *
 * @param modifier 装饰器
 * @param labelName label名称
 * @param labelWidth label宽度
 * @param defaultDate 默认时间 当defaultDate为空时，显示请选择
 * @param showTime 是否显示时分秒 默认false 时分秒只显示00:00:00 ~ 23:59:59
 * @param onConfirm 确定回调
 * p1:日期[yyyy-MM-dd],
 * @param onCancel 取消回到
 */
@Composable
fun DateLabel(
    modifier: Modifier = Modifier,
    labelName: String,
    labelWidth: Dp,
    defaultDate: String = "",
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var text by remember {
        mutableStateOf(if (defaultDate.isEmpty()) "" else defaultDate)
    }
    Label(
        modifier = modifier, labelWidth = labelWidth, labelName = labelName, content = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .noEffectClickable {
                        context.showDialog {
                            DatePicker(
                                defaultDate = defaultDate,
                                onConfirm = {
                                    text = it
                                    onConfirm(it)
                                    context.hideDialog()
                                },
                                onCancel = {
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
                        style = MaterialTheme.typography.bodyMedium,
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
            DateLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelWidth = 60.dp,
                labelName = "date",
                onConfirm = {}
            )
        }
    }
}