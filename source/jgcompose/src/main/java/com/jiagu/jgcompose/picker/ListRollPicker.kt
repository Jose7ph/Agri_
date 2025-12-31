package com.jiagu.jgcompose.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.screen.ScreenColumn
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 滚动效果的文本List picker
 * @param index 当前索引
 * @param items 列表项 item中内容有重复则定位到第一个元素位置
 * @param onCancel 取消回调
 * @param onConfirm 确定回调 返回当前选择的值
 */
@Composable
fun ListRollPicker(
    index: Int,
    items: List<String>,
    itemTips: List<String> = listOf(),
    onCancel: () -> Unit,
    onConfirm: (Int, String) -> Unit,
) {
    var selectedIndex by remember {
        mutableIntStateOf(index)
    }
    var selectedItem by remember {
        mutableStateOf(items.getOrElse(index) { "" })
    }
    LaunchedEffect(index) {
        selectedIndex = index
        selectedItem = items.getOrElse(index) { "" }
    }
    ScreenColumn(content = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RollPicker(
                modifier = Modifier.fillMaxWidth(),
                idx = selectedIndex,
                value = selectedItem,
                item = { idx, text ->
                    Row {
                        Label(
                            text = text,
                        )
                        if (itemTips.getOrNull(idx)?.isNotEmpty() == true) {
                            Label(
                                text = itemTips[idx],
                                color = Color.Red,
                            )
                        }
                    }
                },
                list = items.toList(),
                onValueChange = { i, v ->
                    selectedIndex = i
                    selectedItem = v
                })
            // 中间两道横线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center)
            ) {
                HorizontalDivider(Modifier.padding(horizontal = 15.dp))
                HorizontalDivider(
                    Modifier
                        .padding(horizontal = 15.dp)
                        .align(Alignment.BottomStart)
                )
            }
        }
    }, onCancel = onCancel, onConfirm = {
        onConfirm(selectedIndex, selectedItem)
    })
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ListRollPickerPreview() {
    ComposeTheme {
        ListRollPicker(
            index = 5, listOf(
            "111",
            "222",
            "333",
            "555",
            "555",
        ), onCancel = {}, onConfirm = { _, _ -> })

    }
}