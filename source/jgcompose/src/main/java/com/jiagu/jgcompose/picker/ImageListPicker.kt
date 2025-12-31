package com.jiagu.jgcompose.picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.screen.ScreenColumn
import com.jiagu.jgcompose.shadow.ShadowFrame
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
 * @param images 图片，需要跟items 一致 不然会报错
 * @param imageColorChange 图片颜色转换 false时 图片不会根据选中效果改变颜色
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 * @receiver
 */
@Composable
fun ImageListPicker(
    rowItemNum: Int,
    selectedIndexes: List<Int>,
    images: List<Int>,
    items: List<String>,
    itemHeight: Dp = 30.dp,
    isSingleSelect: Boolean = true,
    imageColorChange: Boolean = true,
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
    ScreenColumn(content = {
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
                val image = images[it]
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    ShadowFrame {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                )
                                .fillMaxWidth()
                                .height(itemHeight)
                                .padding(horizontal = 6.dp)
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
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = image),
                                contentDescription = "image $it",
                                modifier = Modifier.size(itemHeight * 0.9f),
                                colorFilter = if (imageColorChange) {
                                    if (isSelected) null else ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                }
                            )
                            AutoScrollingText(
                                text = item,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) Color.White else Color.Black,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }, onCancel = onCancel, onConfirm = {
        onConfirm(selected)
    })
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ListPickerPreview() {
    ComposeTheme {
        Column {
            ImageListPicker(rowItemNum = 5, selectedIndexes = listOf(1, 2, 3), items = listOf(
                "111",
                "222",
                "333",
                "444",
                "555",
                "666",
                "7777",
            ), images = listOf(
                R.drawable.lost,
                R.drawable.lost,
                R.drawable.lost,
                R.drawable.lost,
                R.drawable.lost,
                R.drawable.lost,
                R.drawable.lost,
            ), isSingleSelect = false, onCancel = {}, onConfirm = { },
                imageColorChange = false)
        }
    }
}