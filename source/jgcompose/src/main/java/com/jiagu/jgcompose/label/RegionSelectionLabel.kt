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
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.picker.Address
import com.jiagu.jgcompose.picker.RegionPicker
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 地区选择 label
 *
 * @param modifier 装饰器
 * @param labelName label名称
 * @param labelWidth label的长度
 * @param defaultText 默认显示值 若为""则显示请选择
 * @param regions 地区列表
 * @param onConfirm 确定回调，返回一个Address对象
 * @param onDismiss 取消回调
 */
@Composable
fun RegionSelectionLabel(
    modifier: Modifier = Modifier,
    labelName: String,
    labelWidth: Dp,
    defaultText: String = "",
    regions: List<Address>,
    onConfirm: (Address) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var text by remember {
        mutableStateOf(defaultText)
    }
    Label(
        modifier = modifier,
        labelWidth = labelWidth,
        labelName = labelName,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .noEffectClickable {
                        context.showDialog {
                            RegionPicker(
                                regions = regions,
                                onConfirm = {
                                    text = it.name
                                    onConfirm(it)
                                },
                                onDismiss = {
                                    text = ""
                                    onDismiss()
                                },
                            )
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
                    contentDescription = null,

                    )
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RegionSelectionLabelPreview() {
    ComposeTheme {
        Column {
            RegionSelectionLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelWidth = 60.dp, labelName = "region",
                regions = listOf(
                    Address(1, "北京"),
                    Address(2, "上海"),
                    Address(3, "广州"),
                    Address(4, "深圳"),
                    Address(5, "杭州"),
                    Address(6, "武汉"),
                    Address(7, "南京"),
                    Address(8, "苏州"),
                    Address(9, "成都"),
                    Address(10, "重庆"),
                    Address(11, "西安"),
                    Address(12, "长沙"),
                    Address(13, "郑州"),
                    Address(14, "沈阳"),
                    Address(15, "青岛"),
                    Address(16, "福州"),
                    Address(17, "厦门"),
                    Address(18, "南昌"),
                    Address(19, "合肥"),
                    Address(20, "大连"),
                    Address(21, "哈尔滨"),
                    Address(22, "昆明"),
                    Address(23, "兰州"),
                    Address(24, "西宁"),
                    Address(25, "银川"),
                    Address(26, "乌鲁木齐"),
                ),
                defaultText = "aaa",
                onConfirm = {

                }
            )
        }
    }
}