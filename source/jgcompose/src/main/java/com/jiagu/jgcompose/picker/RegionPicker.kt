package com.jiagu.jgcompose.picker

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.screen.ScreenSearchColumn
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

class Address(
    val code: Int,
    var name: String,
    var child: MutableList<Address> = mutableListOf(),
    var parent: Address? = null,
) {
    override fun toString() = "$name($code)"
}

/**
 * 地区 picker
 *
 * @param regions 地区列表
 * @param onConfirm 确定回调
 * @param onDismiss 取消回调 (可不传)
 */
@Composable
fun RegionPicker(
    regions: List<Address>,
    onConfirm: (Address) -> Unit,
    onDismiss: () -> Unit = {},
) {
    val context = LocalContext.current
    //检索结果列表
    val searchList = remember { mutableStateListOf<Address>() }
    //已选择列表
    val selectedList = remember { mutableStateListOf<Address>() }
    //子集列表
    val childList = remember { mutableStateListOf<Address>() }
    //过滤列表
    var isFilter by remember {
        mutableStateOf(false)
    }
    ScreenSearchColumn(
        paddingTop = 100.dp,
        content = {
            Column {
                //已选择
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) {
                    if (selectedList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            repeat(selectedList.size) {
                                RegionBox(region = selectedList[it], onClick = { selectedRegion ->
                                    val curParent = selectedRegion.parent
                                    //判断删除的是否是最后一级，如果不是则连同后续全部删除
                                    val curLastRegion = selectedList.last()
                                    if (curLastRegion == selectedRegion) {
                                        //删除点击的地区
                                        selectedList.remove(selectedRegion)
                                    } else {
                                        //获取当前元素索引
                                        val curIndex = selectedList.indexOf(selectedRegion)
                                        if (curIndex != -1) {
                                            selectedList.subList(curIndex, selectedList.size)
                                                .clear()
                                        }
                                    }

                                    //判断删除地区是否有父级，有父级则当前显示父级下所有子集
                                    //没有父级说明当前已经是最上级父级
                                    childList.clear()
                                    if (curParent != null) {
                                        childList.addAll(curParent.child)
                                    }
                                })
                            }
                        }
                    }
                }
                //是检索列表
                if (isFilter) {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(searchList.size) {
                            RegionBox(region = searchList[it], onClick = { searchRegion ->
                                //清除已选择列表
                                selectedList.clear()
                                //添加当前的选择 如有父级则一起添加
                                selectedList.addAll(findAndSelectedParent(searchRegion))
                                //后续优先显示当前层级的列表数据 即当前地区的 parent.child
                                childList.clear()
                                //有child 优先显示child
                                if (searchRegion.child.isNotEmpty()) {
                                    childList.addAll(searchRegion.child)
                                }
                                //无child 判断有无parent
                                else {
                                    //有parent 显示parent.child (当前层级的列表)
                                    if (searchRegion.parent != null) {
                                        childList.addAll(searchRegion.parent!!.child)
                                    } else {
                                        //无child 无parent 则只显示自己
                                        childList.add(searchRegion)
                                    }
                                }
                                //清除当前过滤项列表
                                searchList.clear()
                                isFilter = false
                            })
                        }
                    }

                } else {//不是检索列表
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(if (childList.isNotEmpty()) childList.size else regions.size) {
                            val reg = if (childList.isNotEmpty()) {
                                childList[it]
                            } else {
                                regions[it]
                            }
                            RegionBox(region = reg, onClick = { region ->
                                //有子集 则后续显示子集
                                if (region.child.isNotEmpty()) {
                                    childList.clear()
                                    childList.addAll(region.child)
                                }
                                if (selectedList.isNotEmpty()) {
                                    //判断当前数据是否存在
                                    var curLastRegion = selectedList.last()
                                    //当前选择的父级和最后一次选择的地区父级一致，说明是同级数据，则替换数据
                                    if (curLastRegion.parent == region.parent) {
                                        selectedList.remove(curLastRegion)
                                        selectedList.add(region)
                                    }
                                    curLastRegion = selectedList.last()
                                    //当前选择不存在
                                    if (region != curLastRegion) {
                                        selectedList.add(region)
                                    }
                                } else {
                                    selectedList.add(region)
                                }
                            })
                        }
                    }
                }
            }
        },
        onCancel = {
            onDismiss()
            context.hideDialog()
        },
        onConfirm = {
            if (selectedList.isNotEmpty()) {
                onConfirm(selectedList.last())
            }
            context.hideDialog()
        },
        onSearch = { search ->
            searchList.clear()
            searchList.addAll(findAddress(search, regions))
            isFilter = true
        })
}

/**
 * 地区Box
 */
@Composable
private fun RegionBox(
    region: Address,
    onClick: (Address) -> Unit,
) {
    Box(
        modifier = Modifier
            .widthIn(min = 60.dp, max = 90.dp)
            .height(30.dp)
            .background(
                color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small
            )
            .noEffectClickable {
                onClick(region)
            },
        contentAlignment = Alignment.Center
    ) {
        AutoScrollingText(
            text = region.name,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

private fun findAddress(
    text: String, address: List<Address>,
): List<Address> {
    val out = mutableListOf<Address>()
    findText(text, address, out)
    return out
}

private fun findText(
    text: String, address: List<Address>, out: MutableList<Address>,
) {
    for (addr in address) {
        if (addr.name.lowercase().contains(text.lowercase())) {
            out.add(addr)
        }
        if (addr.child.isNotEmpty()) {
            findText(text, addr.child, out)
        }
    }
}

private fun findAndSelectedParent(
    address: Address,
): MutableList<Address> {
    val selectList = mutableListOf<Address>()
    findParent(address, selectList)
    return selectList
}

private fun findParent(
    address: Address, selectList: MutableList<Address>,
) {
    if (!selectList.contains(address)) {
        selectList.add(address)
    }
    //有父集继续递归
    if (address.parent != null) {
        findParent(address.parent!!, selectList)
    }
    //数量 > 1 重新排序
    if (selectList.size > 1) {
        selectList.sortBy {
            it.code
        }
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RegionPickerPreview() {
    ComposeTheme {
        Column {
            RegionPicker(
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
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}