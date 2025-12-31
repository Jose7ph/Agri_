package com.jiagu.jgcompose.paging

import androidx.annotation.Keep
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/**
 * Page 对象 若要使用paging，必须要使用该对象作为返回值
 *
 * @param T
 * @property total
 * @constructor Create empty Page
 */
@Keep
open class Page<T>(val total: Int) {
    var pages: Int = 0
    var list: List<T> = listOf()
}

/**
 * Paging
 *
 * @param T 泛型
 * @param pageSize 一页数量
 * @param initialLoadSize 初始化一页数量
 * @param enablePlaceholders 占位符 默认true
 * @param api 调用接口
 */
abstract class Paging<T : Any>(
    pageSize: Int,
    initialLoadSize: Int = pageSize,
    enablePlaceholders: Boolean = true,
    api: (PagingSource.LoadParams<Int>) -> Flow<Page<T>>,
) {
    private var repository =
        PageRepository(pageSize, initialLoadSize, enablePlaceholders, api)

    fun load(): Flow<PagingData<T>> {
        return repository.getData()
    }

    // DataSource 内部抽象类，使用 in 投影允许协变使用
    class PageDataSource<T : Any>(
        val api: (LoadParams<Int>) -> Flow<Page<T>>,
    ) : PagingSource<Int, T>() {
        override fun getRefreshKey(state: PagingState<Int, T>): Int? {
            // 根据preKey和nextKey中找到离anchorPosition最近页面的键值
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
            // 定义键值
            val key = params.key ?: 1
            var loadResult: LoadResult<Int, T> = LoadResult.Page(listOf(), 0, 0)
            api(params).catch {
                loadResult = LoadResult.Error(it)
            }.collectLatest {
                loadResult = LoadResult.Page(
                    data = it.list,
                    prevKey = if (key == 1 || it.list.isEmpty()) null else key - 1,
                    nextKey = if (key == it.pages || it.list.isEmpty() || key * params.loadSize >= it.total) null else key + 1
                )
            }
            return loadResult
        }
    }

    class PageRepository<T : Any>(
        private val pageSize: Int,
        private val initialLoadSize: Int = pageSize,
        private val enablePlaceholders: Boolean = true,
        val api: (PagingSource.LoadParams<Int>) -> Flow<Page<T>>,
    ) {
        fun getData(): Flow<PagingData<T>> {
            // 通过Pager.flow返回流对象
            return Pager(
                config = PagingConfig(
                    initialLoadSize = initialLoadSize,
                    pageSize = pageSize,
                    enablePlaceholders = enablePlaceholders
                ), pagingSourceFactory = {
                    PageDataSource(api)
                }).flow
        }
    }
}

/**
 * Paging
 *
 * @param T 泛型
 * @param pageSize 一页数量
 * @param initialLoadSize 初始化一页数量
 * @param enablePlaceholders 占位符 默认true
 * @param api 调用接口
 */
abstract class ListPaging<T : Any>(
    pageSize: Int,
    initialLoadSize: Int = pageSize,
    enablePlaceholders: Boolean = true,
    api: suspend (PagingSource.LoadParams<Int>) -> List<T>,
) {
    private var repository =
        PageRepository(pageSize, initialLoadSize, enablePlaceholders, api)

    fun load(): Flow<PagingData<T>> {
        return repository.getData()
    }

    // DataSource 内部抽象类，使用 in 投影允许协变使用
    class PageDataSource<T : Any>(
        val api: suspend (LoadParams<Int>) -> List<T>,
    ) : PagingSource<Int, T>() {
        override fun getRefreshKey(state: PagingState<Int, T>): Int? {
            // 根据preKey和nextKey中找到离anchorPosition最近页面的键值
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
            // 定义键值
            val key = params.key ?: 1
            var loadResult: LoadResult<Int, T>
            try {
                val list = withContext(Dispatchers.IO) {
                    api(params)
                }
                loadResult = LoadResult.Page(
                    data = list,
                    prevKey = if (key == 1 || list.isEmpty()) null else key - 1,
                    nextKey = if (list.isEmpty() || params.loadSize > list.size) null else key + 1 //返回数据位空 || 返回的数据数量 < 需要加载的数量 则继续加载
                )
            } catch (e: Exception) {
                loadResult = LoadResult.Error(e)
            }
            return loadResult
        }
    }

    class PageRepository<T : Any>(
        private val pageSize: Int,
        private val initialLoadSize: Int = pageSize,
        private val enablePlaceholders: Boolean = true,
        val api: suspend (PagingSource.LoadParams<Int>) -> List<T>,
    ) {
        fun getData(): Flow<PagingData<T>> {
            // 通过Pager.flow返回流对象
            return Pager(
                config = PagingConfig(
                    initialLoadSize = initialLoadSize,
                    pageSize = pageSize,
                    enablePlaceholders = enablePlaceholders
                ), pagingSourceFactory = {
                    PageDataSource(api)
                }).flow
        }
    }
}