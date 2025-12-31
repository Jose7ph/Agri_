package com.jiagu.jgcompose.popup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 列表选择弹窗
 *
 * @param T 列表数据类型泛型
 * @param title 标题 可选
 * @param titleColor 标题颜色 默认 黑
 * @param list 列表数据
 * @param item 列表项的ui组件
 * @param isSingle 是否单选 默认是
 * @param isPosition 是否需要定位 在列表项过多时，默认定位到传入的第一个元素项位置 默认开启
 * @param itemPaddingValues 列表项内padding
 * @param defaultIndexes 默认选项 多选传多个
 * @param confirmText 确定按钮文本
 * @param cancelText 取消按钮文本
 * @param showCancel 确定按钮显示 默认 true
 * @param showConfirm 取消按钮显示 默认 true
 * @param onConfirm 点击确定事件 p1:选择的索引 , p2:选择的元素
 * @param onDismiss 点击取消事件
 */
@Composable
fun <T> ListSelectionPopup(
    title: String? = null,
    titleColor: Color = Color.Black,
    defaultIndexes: List<Int> = emptyList(),
    list: List<T>,
    item: @Composable (T) -> Unit,
    itemPaddingValues: PaddingValues = PaddingValues(horizontal = 30.dp, vertical = 4.dp),
    isSingle: Boolean = true,
    isPosition: Boolean = true,
    confirmText: Int = R.string.confirm,
    cancelText: Int = R.string.cancel,
    showCancel: Boolean = true,
    showConfirm: Boolean = true,
    onConfirm: (List<Int>, List<T>) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedIndexes = remember {
        mutableStateListOf<Int>()
    }
    LaunchedEffect(defaultIndexes) {
        selectedIndexes.clear()
        if (defaultIndexes.isNotEmpty()) {
            //单选
            if (isSingle) {
                //仅添加第一个索引
                selectedIndexes.add(defaultIndexes[0])
            } else {
                //多选 全部添加
                selectedIndexes.addAll(defaultIndexes)
            }
        }
    }
    val lazyListState = rememberLazyListState()
    //用于item定位，只会定位到index = 0的位置
    LaunchedEffect(null) {
        if (selectedIndexes.isNotEmpty() && isPosition) {
            lazyListState.animateScrollToItem(selectedIndexes[0])
        }
    }
    ScreenPopup(
        width = 300.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                //标题
                title?.let {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor,
                        )
                    }
                }
                //有数据显示列表
                if (list.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        state = lazyListState,
                        contentPadding = itemPaddingValues,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        items(list.size) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 30.dp)
                                    .clickable {
                                        //单选
                                        if (isSingle) {
                                            if (selectedIndexes.contains(it)) {
                                                selectedIndexes.remove(it)
                                            } else {
                                                selectedIndexes.clear()
                                                selectedIndexes.add(it)
                                            }
                                        } else {
                                            //多选
                                            if (selectedIndexes.contains(it)) {
                                                selectedIndexes.remove(it)
                                            } else {
                                                selectedIndexes.add(it)
                                            }
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isSingle) {
                                    RadioButton(
                                        selected = selectedIndexes.contains(it), onClick = null
                                    )
                                } else {
                                    Checkbox(
                                        checked = selectedIndexes.contains(it),
                                        onCheckedChange = null
                                    )
                                }
                                item(list[it])
                            }
                        }
                    }
                } else {
                    //无数据显示no_data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_data),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        onConfirm = {
            onConfirm(
                selectedIndexes,
                list.filterIndexed { index, _ -> selectedIndexes.contains(index) })
        },
        onDismiss = onDismiss,
        showConfirm = showConfirm,
        showCancel = showCancel,
        confirmText = confirmText,
        cancelText = cancelText,
        confirmEnable = selectedIndexes.isNotEmpty()
    )
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ListPopupPreview() {
    ComposeTheme {
        Column {
            val list = listOf(
                "1111111", "222222", "333333333", "44444444", "55", "777", "88", "99", "00", "10921"
            )
            ListSelectionPopup(title = "列表", list = list, isSingle = false, item = {
                Text(
                    text = it,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }, onDismiss = {}, onConfirm = { _, _ -> })
        }
    }
}