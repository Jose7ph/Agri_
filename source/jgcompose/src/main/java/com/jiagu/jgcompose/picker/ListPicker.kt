package com.jiagu.jgcompose.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.screen.ScreenColumn
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme


/**
 * 文本List picker
 *
 * @param rowItemNum 一行选项数量
 * @param selectedIndexes  已选择的索引
 * @param items 列表项
 * @param itemHeight 单项高度
 * @param isSingleSelect 是否单选
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 * @receiver
 */
@Composable
fun ListPicker(
    rowItemNum: Int,
    selectedIndexes: List<Int>,
    items: List<String>,
    itemHeight: Dp = 30.dp,
    isSingleSelect: Boolean = true,
    onCancel: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val selected = remember {
        mutableStateListOf<Int>().apply {
            if (selectedIndexes.isNotEmpty()) {
                //单选
                if (isSingleSelect) {
                    //仅添加第一个索引
                    add(selectedIndexes[0])
                } else {
                    //多选 全部添加
                    addAll(selectedIndexes)
                }
            }
        }
    }
    ScreenColumn(
        content = {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(rowItemNum),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(items.size) {
                    val item = items[it]
                    val isSelected = selected.contains(it)
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = MaterialTheme.shapes.small
                                )
                                .fillMaxWidth()
                                .height(itemHeight)
                                .noEffectClickable {
                                    if (isSelected) {
                                        selected.remove(it)
                                    } else {
                                        if (isSingleSelect) {
                                            selected.clear()
                                        }
                                        selected.add(it)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AutoScrollingText(
                                text = item,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        onCancel = onCancel,
        onConfirm = {
            onConfirm(selected)
        }
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ListPickerPreview() {
    ComposeTheme {
        Column {
            ListPicker(
                rowItemNum = 5,
                selectedIndexes = listOf(1, 2, 3),
                items = listOf(
                    "111",
                    "222",
                    "333",
                    "444",
                    "555",
                    "555",
                    "666",
                    "7777",
                    "88888",
                    "99999",
                    "12321555553",
                    "7777",
                    "88888",
                    "99999",
                    "12321555553",
                    "7777",
                    "88888",
                    "99999",
                    "12321555553",
                ),
                isSingleSelect = false,
                onCancel = {
                },
                onConfirm = { }
            )
        }
    }
}