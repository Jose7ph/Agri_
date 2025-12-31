package com.jiagu.ags4.vm.work

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.paging.PagingData
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.api.helper.GeoHelper
import com.jiagu.jgcompose.paging.ListPaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow


interface IWorkPage {
    var isLoading: Boolean
    var pageDataInit: Boolean
    var localBlocksPageList: Flow<PagingData<BlockPlan>>
    val localBlocksListFlow: MutableStateFlow<List<BlockPlan>>
    val localBlocksCanvasFlow: MutableSharedFlow<List<BlockPlan>>
    val highlightBlocks: MutableStateFlow<List<BlockPlan>>
    val highlightedBlocks: SnapshotStateList<BlockPlan>
    var search: String
    var timeSort: Int
    var blockState: Int
    var syncStatus: SyncStatusEnum
    suspend fun refreshList(
        position: GeoHelper.LatLngAlt,
        loadSize: Int = 20,
        meter: Float = 3000f,
        loadLocalComplete: () -> Unit = {},
        updateNetComplete: () -> Unit = {},
    )

    suspend fun refreshPage(position: GeoHelper.LatLngAlt, loadSize: Int = 20, meter: Float = 3000f)
    fun setHighlightBlocksByUniqueIds(uniqueIds: List<String>)
    fun stopAndClearCanvasFlow()
    suspend fun clearAndPushCanvasFlow()

    fun updateBlocksList(blockPlans: List<BlockPlan>)
}

class WorkPageImpl(val blockType: Int) : IWorkPage {
    override var isLoading by mutableStateOf(false) // 用于控制本地数据库加载状态，本地数据读取完成后设置为false
    override var pageDataInit by mutableStateOf(true)
    override var localBlocksPageList by mutableStateOf<Flow<PagingData<BlockPlan>>>(emptyFlow())
    override val localBlocksListFlow =
        MutableStateFlow<List<BlockPlan>>(emptyList()) //所有地块数据，每次收到的数据一定是当前的全部数据
    override val localBlocksCanvasFlow =
        MutableSharedFlow<List<BlockPlan>>(
            replay = 10,
            extraBufferCapacity = 100
        ) //canvas绘制用的地块数据，每次收到的数据数量可能不一致
    override val highlightBlocks = MutableStateFlow<List<BlockPlan>>(emptyList())
    override val highlightedBlocks = mutableStateListOf<BlockPlan>()
    override var search by mutableStateOf("") //检索用 地块名称
    override var timeSort by mutableIntStateOf(1) //1 -> DESC 2 -> ASC 3 -> not sort
    override var blockState by mutableIntStateOf(0) //0 -> all 1 -> 进行中 2 -> 已完成
    override var syncStatus by mutableStateOf(SyncStatusEnum.NONE) //网络同步状态控制 1 -> 同步中 2 -> 同步完成 3 -> 同步失败

    override suspend fun refreshList(
        position: GeoHelper.LatLngAlt,
        loadSize: Int,
        meter: Float,
        loadLocalComplete: () -> Unit,
        netUpdateComplete: () -> Unit,
    ) {
        if (!isLoading) {
            isLoading = true
            val to = System.currentTimeMillis()
            val from = to - 24 * 3600 * 7 * 1000
            val lat = position.latitude
            val lng = position.longitude
            val off = GeoHelper.boundOffset(GeoHelper.LatLng(lat, lng), meter)
            val n = lat + off.latitude
            val s = lat - off.latitude
            val e = lng + off.longitude
            val w = lng - off.longitude
            val allDataList = mutableListOf<BlockPlan>()
            localBlocksListFlow.emit(emptyList())
            stopAndClearCanvasFlow()
            //先获取本地数据更新canvas
            Repo.getAllNearbyBlocks(
                n = n,
                s = s,
                e = e,
                w = w,
                from = from,
                to = to,
                blockType = blockType,
                batchSize = 100,
                batchData = {
                    val filterList = filterList(it)
                    localBlocksCanvasFlow.emit(filterList)
                    allDataList.addAll(filterList)
                    val sortList = sortList(allDataList)
                    localBlocksListFlow.emit(sortList.toList())
                },
                complete = {
                    loadLocalComplete()
                    isLoading = false
//                    Log.d("zhy", "init list data: ${filterList.size}")
                }

            )
            //启动后台更新地块逻辑
            if (syncStatus != SyncStatusEnum.SYNCING) {
                updateLocalBlocks(
                    n = n,
                    s = s,
                    e = e,
                    w = w,
                    from = from,
                    to = to,
                    insertComplete = {
                        //新增仅过滤数据，不排序 最后统一排序
                        if (it.isNotEmpty()) {
                            val filterList = filterList(it)
                            localBlocksCanvasFlow.emit(filterList)
                            allDataList.addAll(filterList)
                            localBlocksListFlow.emit(allDataList.toList())
                            Log.d("zhy", "insertComplete: ${localBlocksListFlow.value.size}")
//                        Log.d("zhy", "insert new list data: ${filterList.size}")
                        }
                    },
                    updateComplete = {
                        for (block in it) {
                            val index = allDataList.indexOfFirst { item ->
                                item.uniqueId() == block.uniqueId()
                            }
                            if (index != -1) {
                                allDataList[index] = block // 直接替换指定位置的元素
                            }
                        }
//                        Log.d("zhy", "update old list data: ${it.size}")
                    },
                    taskComplete = {
                        //localBlocksList 排序
                        val sortList = sortList(allDataList)
                        localBlocksListFlow.value = sortList
                        Log.d("zhy", "taskComplete: ${localBlocksListFlow.value.size}")
                        netUpdateComplete()
//                        Log.d("zhy", "list loading end, all data size: ${allDataList.size}")
                    }
                )
            }
        }
    }

    private fun filterList(dataList: List<BlockPlan>): List<BlockPlan> {
        if (dataList.isEmpty()) return emptyList()
        var newList = dataList.toList()
        //如果有blockState 先过滤blockState筛选掉大部分数据，再过滤search
        when (blockState) {
            1 -> newList = newList.filter { it.working }
            2 -> newList = newList.filter { it.finish }
        }
        //过滤名称
        if (search.isNotEmpty()) {
            newList = newList.filter { it.blockName.contains(search) }
        }
        //根据uniqueId去重
        return newList.distinctBy { it.uniqueId() }
    }

    private fun sortList(dataList: List<BlockPlan>): List<BlockPlan> {
        if (dataList.isEmpty()) return emptyList()
        return when (timeSort) {
            1 -> dataList.sortedByDescending { it.createTime } // 倒序排列
            2 -> dataList.sortedBy { it.createTime }           // 正序排列
            else -> dataList                                   // 保持原顺序
        }
    }

    /**
     * 停止并清除当前canvas正在画的地块数据
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun stopAndClearCanvasFlow() {
        localBlocksCanvasFlow.resetReplayCache() //MutableSharedFlow 需要通过reset清空里面已存在的值
    }

    /**
     * 清除当前canvas正在绘制的数据，并将本地数据推送到canvas
     *
     */
    override suspend fun clearAndPushCanvasFlow() {
        val localBlocksList = localBlocksListFlow.value
        if (localBlocksList.isNotEmpty()) {
            stopAndClearCanvasFlow()
            localBlocksCanvasFlow.emit(localBlocksList)
        }
    }

    /**
     * 更新所有blockplan 列表
     *
     * @param blockPlans
     */
    override fun updateBlocksList(blockPlans: List<BlockPlan>) {
        if (blockPlans.isEmpty()) return
        val olbl = localBlocksListFlow.value.toMutableList()
        for (blockPlan in blockPlans) {
            val index = olbl.indexOfFirst { it.localBlockId == blockPlan.localBlockId }
            if (index != -1) {
                // 直接替换整个对象
                olbl[index] = blockPlan
                // 同时更新高亮地块
                highlightBlocks.value.indexOfFirst { it.localBlockId == blockPlan.localBlockId }
                    .takeIf { it != -1 }
                    ?.let { highlightBlocks.value = highlightBlocks.value.toMutableList().apply { set(it, blockPlan) } }
                highlightedBlocks.indexOfFirst { it.localBlockId == blockPlan.localBlockId }
                    .takeIf { it != -1 }
                    ?.let { highlightedBlocks[it] = blockPlan }
            }
        }
        // 更新列表数据
        localBlocksListFlow.value = olbl
    }

    /**
     * 刷新地块（分页）
     *
     * @param position 当前位置
     * @param loadSize 单次加载数量 默认20天
     * @param meter 查询范围 默认 3000
     * @receiver
     */
    override suspend fun refreshPage(
        position: GeoHelper.LatLngAlt,
        loadSize: Int,
        meter: Float,
    ) {
        val to = System.currentTimeMillis()
        val from = to - 24 * 3600 * 7 * 1000
        val lat = position.latitude
        val lng = position.longitude
        val off = GeoHelper.boundOffset(GeoHelper.LatLng(lat, lng), meter)
        val n = lat + off.latitude
        val s = lat - off.latitude
        val e = lng + off.longitude
        val w = lng - off.longitude
        val localBlocksPage = LocalBlocksPage(
            n = n,
            s = s,
            e = e,
            w = w,
            from = from,
            to = to,
            blockType = blockType,
            blockName = search,
            blockState = blockState,
            order = when (timeSort) {
                1 -> "DESC"
                2 -> "ASC"
                else -> ""
            },
            loadSize = loadSize
        )
        //新增/更新地块
        if (syncStatus != SyncStatusEnum.SYNCING) {
            updateLocalBlocks(
                n = n,
                s = s,
                e = e,
                w = w,
                from = from,
                to = to,
                taskComplete = {
                    //更新后加载本地列表
                    localBlocksPageList = localBlocksPage.load()
                }
            )
        }
    }

    suspend fun updateLocalBlocks(
        n: Double,
        s: Double,
        e: Double,
        w: Double,
        to: Long,
        from: Long,
        insertComplete: suspend (List<BlockPlan>) -> Unit = {},
        updateComplete: suspend (List<BlockPlan>) -> Unit = {},
        taskComplete: () -> Unit = {},
    ) {
        syncStatus = SyncStatusEnum.SYNCING
        Repo.updateAllLocalBlocks(
            n = n,
            s = s,
            e = e,
            w = w,
            from = from,
            to = to,
            blockType = blockType,
            insertComplete = insertComplete,
            updateComplete = updateComplete,
            taskComplete = { success ->
                syncStatus = if (success) {
                    SyncStatusEnum.SUCCESS
                } else {
                    SyncStatusEnum.FAILED
                }
                taskComplete()
            }
        )
    }

    private class LocalBlocksPage(
        n: Double,
        s: Double,
        e: Double,
        w: Double,
        from: Long,
        to: Long,
        blockType: Int? = null,
        blockName: String,
        blockState: Int,
        order: String,
        loadSize: Int = 20,
    ) : ListPaging<BlockPlan>(
        pageSize = loadSize,
        api = { params ->
            AgsDB.getNearbyBlocksByPage(
                w, e, s, n,
                from, to,
                params.loadSize,
                params.key ?: 0,
                blockType,
                blockName,
                blockState,
                order
            )
        },
    )

    override fun setHighlightBlocksByUniqueIds(uniqueIds: List<String>) {
        val allBlocks = localBlocksListFlow.value
        highlightedBlocks.clear()
        val filterIds = uniqueIds.filter { !it.startsWith("zone") }
        val out = mutableListOf<BlockPlan>()
        for (uniqueId in filterIds) {
            allBlocks.find { it.uniqueId() == uniqueId }?.let {
                out.add(it)
            }
        }
        highlightBlocks.value = out.toList()
        highlightedBlocks.addAll(out)
    }
}

enum class SyncStatusEnum {
    NONE,
    SYNCING,
    SUCCESS,
    FAILED
}