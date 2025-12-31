package com.jiagu.jgcompose.paging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jiagu.jgcompose.theme.ComposeTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 带下拉刷新的列表
 *
 * @param T 泛型
 * @param modifier 装饰器
 * @param contentPadding 内padding
 * @param verticalArrangement 垂直居中
 * @param horizontalAlignment 水平居中
 * @param items 列表项
 * @param item 选项
 * @param pullToRefreshEnabled 是否启用下拉
 * @param autoInitData 自动初始化数据
 * @param autoInitDataCallback 自动初始化数据回调
 * @param onRefresh 刷新回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> LazyColumnPaging(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    items: Flow<PagingData<T>>,
    item: @Composable (T) -> Unit,
    pullToRefreshEnabled: Boolean = true,
    autoInitData: Boolean = false,
    autoInitDataCallback: () -> Unit = {},
    onRefresh: () -> Unit,
) {
    val dataList = items.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing = dataList.loadState.refresh == LoadState.Loading
    //初始化数据用
    DisposableEffect(Unit) {
        if (autoInitData) {
            onRefresh()
            autoInitDataCallback()
        }
        onDispose {

        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (pullToRefreshEnabled) {
                    Modifier.pullToRefresh(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            onRefresh()
                        })
                } else {
                    Modifier
                }
            ), contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            // 监听列表滚动到底部，并触发加载更多数据的操作
            items(dataList.itemCount) { idx ->
                val data = dataList[idx] ?: return@items
                item(data)
            }
        }
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = isRefreshing,
            state = pullToRefreshState
        )
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun LazyColumnPagingPreview() {
    ComposeTheme {
        Column {
            LazyColumnPaging(items = flowOf(PagingData.empty()), item = { _ ->
            }, onRefresh = {})
        }
    }
}