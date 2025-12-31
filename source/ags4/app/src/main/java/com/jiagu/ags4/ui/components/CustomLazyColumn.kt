package com.jiagu.ags4.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * 分页查询LazyColumn
 * @param initPageIndex 初始化页码 默认1
 * @param initPageSize 初始化1页数据数量 默认20
 * @param listDataTotal 数据总个数(全部数据数个，不是当前查询到的数量) 一般存放于viewmodel
 * @param listData 当前已查询到数据 一般存放于viewmodel
 * @param refreshFlag 数据刷新标示 用于控制refreshData方法执行
 * @param refreshData 数据刷新方法 用户刷新列表数据
 * @param initData 数据初始化方法 不传默认与refreshData一致 用于列表数据初始化
 * @param loadMore 加载更多数据方法 当剩余数据不够时，不会执行
 * @param disposableFlag 数据清理标志 当标志为true时 组件销毁后会执行clearData，为false时 则不会执行clearData，保留当前已加载的数据
 * (PS：设置为false时需要注意pageIndex的值，因为pageIndex会跟随组件销毁后重新创建后重新初始化，若viewModel中数据不清理则会导致加载重复数据)
 * @param clearData 数据清理方法 主要用于下拉刷新和组件销毁时，清理viewModel中存储的listData和listDataTotal
 * @param item 需要渲染的内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LazyPageColumn(
    modifier: Modifier = Modifier,
    threshold: Dp = 40.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    initPageIndex: Int = 1,
    initPageSize: Int = 20,
    listDataTotal: Int,
    listData: List<T>,
    refreshFlag: MutableState<Boolean>,
    refreshData: ((Boolean) -> Unit) -> Unit,
    initData: ((Boolean) -> Unit) -> Unit = refreshData,
    loadMore: (Int, (Boolean) -> Unit) -> Unit,
    disposableFlag: Boolean = true,
    clearData: () -> Unit,
    item: @Composable (T) -> Unit,
) {
    //数据加载标识 当前有加载数据方法执行时为 true 因为组件渲染时初始化数据 所以默认值为true
    var loading by rememberSaveable {
        mutableStateOf(true)
    }
    //数据初始化标识 默认false 当initData执行时 变成true
    var initFlag by rememberSaveable {
        mutableStateOf(false)
    }
    var pageIndex by rememberSaveable {
        mutableIntStateOf(initPageIndex)
    }
    val pageSize by remember {
        mutableIntStateOf(initPageSize)
    }
    var pullRefresh by remember {
        mutableStateOf(false)
    }
    val state = rememberLazyListState()
    val refreshState = rememberPullToRefreshState()
    //刷新判断 当刷新标识发生变化且为true时，执行刷新数据方法
    LaunchedEffect(key1 = refreshFlag.value) {
        if (refreshFlag.value) {
            Log.d("zhy", "刷新数据.............................");
            loading = true
            pageIndex = initPageIndex
            refreshData {
                loading = !it
                refreshFlag.value = false
                Log.d(
                    "zhy",
                    "数据刷新结束..数据总共${listDataTotal}条，当前已加载数据共:${listData.size}条,当前已加载${pageIndex}页"
                )
            }
            state.scrollToItem(1)
        }
    }
    //数据销毁 当组件完全销毁时才会执行
    DisposableEffect(Unit) {
        if (!initFlag) {
            initFlag = true
            clearData()
            initData {
                loading = !it
            }
        }
        onDispose {
            if (disposableFlag) {
                clearData()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pullToRefresh(
                state = refreshState, isRefreshing = pullRefresh, onRefresh = {
                    Log.d("zhy", "下拉刷新.........");
                    //先执行clearData后清除viewModel中的数据，listData会变成空，isRefreshing控制的加载中图标就会显示
                    //然后执行refreshData开始获取数据
                    clearData()
                    pageIndex = 1
                    loading = true
                    pullRefresh = true
                    refreshData {
                        loading = !it
                        refreshFlag.value = false
                        pullRefresh = false
                    }
                }, enabled = !loading, //正在获取数据时，不允许下拉刷新 防止多次触发下拉刷新
                threshold = threshold
            ), contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            state = state
        ) {
            itemsIndexed(listData) { index, rowData ->
                item(rowData)
                //加载更多
                LaunchedEffect(key1 = listData.size) {
                    //当前显示的index < 总数量 - 1页的数量的一半(例如一页20条数据,加载到10条数据的时候就会开始加载下一页数据) && 当前不是正在加载中 && 总数量 > 1页的数量  && 当前以获取的数据数量 < 数据总量
                    if (listData.size - index <= pageSize / 2 && !loading && listDataTotal > pageSize * pageIndex && listData.size < listDataTotal) {
                        loading = true
                        //进入之后默认已经初始化了数据，所以loadMore触发相当于加载第二页的数据
                        pageIndex++
                        Log.d(
                            "zhy",
                            "开始加载第${pageIndex}页数据..数据总共${listDataTotal}条，当前已加载数据:${listData.size}条"
                        )
                        loadMore(pageIndex) {
                            loading = !it
                            Log.d(
                                "zhy",
                                "第${pageIndex}页数据加载结束..数据总共${listDataTotal}条，当前已加载数据共:${listData.size}条,当前已加载${pageIndex}页"
                            )
                        }
                    }
                }
            }
        }
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = pullRefresh,
            state = refreshState
        )
    }
}

/**
 * 分页查询LazyVerticalGrid
 * @param initPageIndex 初始化页码 默认1
 * @param initPageSize 初始化1页数据数量 默认20
 * @param listDataTotal 数据总个数(全部数据数个，不是当前查询到的数量) 一般存放于viewmodel
 * @param listData 当前已查询到数据 一般存放于viewmodel
 * @param refreshFlag 数据刷新标示 用于控制refreshData方法执行
 * @param refreshData 数据刷新方法 用户刷新列表数据
 * @param initData 数据初始化方法 不传默认与refreshData一致 用于列表数据初始化
 * @param loadMore 加载更多数据方法 当剩余数据不够时，不会执行
 * @param disposableFlag 数据清理标志 当标志为true时 组件销毁后会执行clearData，为false时 则不会执行clearData，保留当前已加载的数据
 * (PS：设置为false时需要注意pageIndex的值，因为pageIndex会跟随组件销毁后重新创建后重新初始化，若viewModel中数据不清理则会导致加载重复数据)
 * @param clearData 数据清理方法 主要用于下拉刷新和组件销毁时，清理viewModel中存储的listData和listDataTotal
 * @param item 需要渲染的内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LazyPageVerticalGrid(
    modifier: Modifier = Modifier,
    columns: GridCells,
    threshold: Dp = 40.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initPageIndex: Int = 1,
    initPageSize: Int = 20,
    listDataTotal: Int,
    listData: List<T>,
    refreshFlag: MutableState<Boolean>,
    refreshData: ((Boolean) -> Unit) -> Unit,
    initData: ((Boolean) -> Unit) -> Unit = refreshData,
    loadMore: (Int, (Boolean) -> Unit) -> Unit,
    disposableFlag: Boolean = true,
    clearData: () -> Unit,
    item: @Composable (T) -> Unit,
) {
    //数据加载标识 当前有加载数据方法执行时为 true 因为组件渲染时初始化数据 所以默认值为true
    var loading by rememberSaveable {
        mutableStateOf(true)
    }
    //数据初始化标识 默认false 当initData执行时 变成true
    var initFlag by rememberSaveable {
        mutableStateOf(false)
    }
    var pageIndex by rememberSaveable {
        mutableIntStateOf(initPageIndex)
    }
    val pageSize by remember {
        mutableIntStateOf(initPageSize)
    }
    var pullRefresh by remember {
        mutableStateOf(false)
    }
    val state = rememberLazyGridState()
    val refreshState = rememberPullToRefreshState()

    //刷新判断 当刷新标识发生变化且为true时，执行刷新数据方法
    LaunchedEffect(key1 = refreshFlag.value) {
        if (refreshFlag.value) {
            Log.d("zhy", "刷新数据.............................");
            loading = true
            pageIndex = initPageIndex
            refreshData {
                loading = !it
                refreshFlag.value = false
                Log.d(
                    "zhy",
                    "数据刷新结束..数据总共${listDataTotal}条，当前已加载数据共:${listData.size}条,当前已加载${pageIndex}页"
                )
            }
            state.scrollToItem(1)
        }
    }
    //数据销毁 当组件完全销毁时才会执行
    DisposableEffect(Unit) {
        if (!initFlag) {
            initFlag = true
            clearData()
            initData {
                loading = !it
            }
        }
        onDispose {
            if (disposableFlag) {
                clearData()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pullToRefresh(
                state = refreshState, isRefreshing = pullRefresh, onRefresh = {
                    Log.d("zhy", "下拉刷新.........");
                    //先执行clearData后清除viewModel中的数据，listData会变成空，isRefreshing控制的加载中图标就会显示
                    //然后执行refreshData开始获取数据
                    clearData()
                    pageIndex = 1
                    loading = true
                    pullRefresh = true
                    refreshData {
                        loading = !it
                        refreshFlag.value = false
                        pullRefresh = false
                    }
                }, enabled = !loading, //正在获取数据时，不允许下拉刷新 防止多次触发下拉刷新
                threshold = threshold
            ), contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            contentPadding = contentPadding,
            state = state
        ) {
            itemsIndexed(listData) { index, rowData ->
                item(rowData)
                //加载更多
                LaunchedEffect(key1 = listData.size) {
                    //当前显示的index < 总数量 - 1页的数量的一半(例如一页20条数据,加载到10条数据的时候就会开始加载下一页数据) && 当前不是正在加载中 && 总数量 > 1页的数量  && 当前以获取的数据数量 < 数据总量
                    if (listData.size - index <= pageSize / 2 && !loading && listDataTotal > pageSize * pageIndex && listData.size < listDataTotal) {
                        Log.d(
                            "zhy",
                            "开始加载数据..数据总共${listDataTotal}条，当前已加载数据:${listData.size}条, 当前已加载${pageIndex}页"
                        )
                        loading = true
                        //进入之后默认已经初始化了数据，所以loadMore触发相当于加载第二页的数据
                        pageIndex++
                        loadMore(pageIndex) {
                            loading = !it
                            Log.d(
                                "zhy",
                                "第${pageIndex}页数据加载结束..数据总共${listDataTotal}条，当前已加载数据共:${listData.size}条,当前已加载${pageIndex}页"
                            )
                        }
                    }
                }
            }
        }
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = pullRefresh,
            state = refreshState
        )
    }
}