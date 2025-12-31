package com.jiagu.ags4.repo

import android.util.Log
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.bean.SortieAdditional
import com.jiagu.ags4.bean.SortieRoute
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.db.LocalBlockBreakpoint
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

object Repo {

    fun uploadBlocks(list: List<Block>): Flow<List<Long>> {
        return flow {
            val ids = mutableListOf<Long>()
            ids.addAll(AgsDB.saveBlocks(list))
            AgsDB.sync()
            emit(ids)
        }.catch {
            Log.v("yuhang", "upload block err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun updateBlockAndClearPlan(block: Block): Flow<Block> {
        return flow {
            AgsDB.updateBlock(block, true)
            emit(block)
            emitSync()
        }.catch {
            Log.v("yuhang", "local update block err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun getBlockDetail(id: Long): Flow<Block> {
        return flow {
            val block = AgsDB.getBlockDetail(id)
            if (block != null) {
                emit(block)
            } else {
                Log.v("zhy", "local get block detail err: null")
            }
        }.catch {
            Log.e("zhy", "local get block detail err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun updateBlockName(block: Block): Flow<Block> {
        return flow {
            AgsDB.updateBlock(block, false)
            emit(block)
            emitSync()
        }.catch {
            Log.e("zhy", "local update block err: $it")
        }.flowOn(Dispatchers.IO)
    }

    var updateBlockState = false
    suspend fun getNearByBlock(
        n: Double,
        s: Double,
        e: Double,
        w: Double,
        from: Long,
        to: Long,
        blockType: Int?,
        showComplete: suspend (Boolean, List<BlockPlan>) -> Unit,
        allComplete: suspend (Boolean, List<BlockPlan>) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val localBlocks = mutableListOf<BlockPlan>()
                val allBlocks = mutableListOf<BlockPlan>()
                var page = 0
                while (true) {
                    val temp = AgsDB.getNearbyBlocksByPage(
                        w,
                        e,
                        s,
                        n,
                        from,
                        to,
                        100,
                        page * 100,
                        blockType
                    )
                    localBlocks.addAll(temp)
                    if (temp.size < 100) break
                    page++
                }
                //本地查到的地块
                if (localBlocks.isNotEmpty()) {
                    allBlocks.addAll(localBlocks)
                    allComplete(true, allBlocks.toList())
                    showComplete(true, allBlocks.toList())
                }

                LogFileHelper.log("local get nearby block size: ${localBlocks.size}")
                //后台数据更新
                if (!updateBlockState) {
                    updateBlockState = true
                    val blockIds =
                        AgsNet.nearbyBlocks(n, s, e, w, from, to, blockType?.toString() ?: "")

                    val blocks = mutableListOf<Long>()
                    for (netBlock in blockIds) {
                        val localBlock = localBlocks.find { lb -> lb.blockId == netBlock.blockId }
                        if (localBlock == null) {
                            blocks.add(netBlock.blockId)
                        } else if (localBlock.working == false && (//如果本地有这块地，且地块不是在作业中
                                    (localBlock.updateTime != null && localBlock.updateTime!! < netBlock.updateTime!!) //并且本地地块更新时间小于网上更新时间
                                            || (localBlock.regionName == null || localBlock.regionName!!.isEmpty())//或者本地地块没有region信息
                                    )
                        ) {//需要更新地块
                            blocks.add(netBlock.blockId)
                        }
                    }

                    LogFileHelper.log("AgsNet nearby blocks: ${blocks.size}")
                    val step = 100
                    if (blocks.isNotEmpty()) {
                        var index = 0
                        while (index < blocks.size) {
                            val blockIds = arrayListOf<Long>()
                            val maxIndex =
                                if (index + step < blocks.size) index + step else blocks.size
                            for (i in index until maxIndex) {
                                blockIds.add(blocks[i])
                            }
//                            Log.v("shero", "load net block: ${blockIds.size}")
                            LogFileHelper.log("load net block: from $from to $to")
                            val infos = AgsNet.nearbyBlockInfo(blockIds.toTypedArray(), from, to)
                            if (infos.isNotEmpty()) {
                                val insertBlockList = mutableListOf<BlockPlan>()
                                val updateBlockList = mutableListOf<BlockPlan>()
                                for (b in infos) {
                                    val localBlock = AgsDB.getBlockIsExit(b.blockId)
                                    if (localBlock?.delete == 1 || localBlock?.edit == 1) continue
                                    if (localBlock == null) {//本地没有这块地
                                        insertBlockList.add(b)
                                    } else {
                                        b.localBlockId = localBlock._id
                                        b.localPlanId = localBlock.localPlanId
                                        updateBlockList.add(b)
                                    }

                                }
//                                logToFile("insert block:${insertBlockList.size} update block:${updateBlockList.size}")
                                LogFileHelper.log("insert block:${insertBlockList.size} update block:${updateBlockList.size}")
                                //新增的地块
                                if (insertBlockList.isNotEmpty()) {
                                    LogFileHelper.log("insertBlockList begin")
                                    val insertBlockListIds = AgsDB.saveBlockPlans(insertBlockList)
                                    val plans = mutableListOf<Plan>()
                                    for ((i, b) in insertBlockList.withIndex()) {
                                        val localBlockId = insertBlockListIds[i]._id
                                        b.localBlockId = localBlockId
                                        if (b.planId != 0L) {//这个地块有plan
                                            val lp =
                                                AgsDB.getPlanByRemoteId(b.planId)//看本地有没有存过这个plan
                                            if (lp == null) {//本地没有存过这个plan
                                                if (b.plan != null) {
                                                    insertBlockListIds[i].also {
                                                        it.planId = b.plan!!.planId
                                                        it.planRoutes = SortieRoute(b.plan!!.track)
                                                        it.naviArea = b.plan!!.naviArea
                                                        it.workDrug = 0.0
                                                        it.workArea = 0.0
                                                        it.working = 0
                                                        it.finish = 0
                                                        it.additional = null
                                                        it.workRoutes = null
                                                        it.region = b.region ?: 0
                                                        it.regionName = b.regionName ?: ""
                                                    }
                                                    plans.add(b.plan!!)
                                                }
                                            }
                                        }
                                    }
                                    val localPlans = AgsDB.savePlans(plans)
                                    for (p in localPlans) {
                                        insertBlockList.find { it.planId == p.planId }?.also {
                                            it.localPlanId = p._id
                                            it.plan?.localPlanId = p._id
                                        }
                                        insertBlockListIds.find { it.planId == p.planId }?.also {
                                            it.localPlanId = p._id
                                        }
                                    }
                                    allBlocks.addAll(insertBlockList) //将新增地块放入全部地块中
                                    allComplete(true, allBlocks.toList())
                                    showComplete(true, insertBlockList.toList()) //加载新增地块数据
                                    LogFileHelper.log("insertBlockList end")
                                }
                                //更新的地块
                                if (updateBlockList.isNotEmpty()) {
                                    LogFileHelper.log("updateBlockList begin")
                                    AgsDB.updateBlockPlans(updateBlockList, 0)
                                    LogFileHelper.log("updateBlockList end")

                                    for (b in updateBlockList) {
                                        if (b.planId != 0L) {//这个地块有plan
                                            val lp =
                                                AgsDB.getPlanByRemoteId(b.planId)//看本地有没有存过这个plan
                                            if (lp == null) {//本地没有存过这个plan
                                                if (b.plan != null) {
                                                    b.plan?.localBlockId = b.localBlockId
                                                    val localPlanId =
                                                        AgsDB.savePlan(b.plan!!)//保存到本地
                                                    b.localPlanId =
                                                        localPlanId//更新要保存的blockplan的localPlanId
                                                    b.plan?.localPlanId = localPlanId
                                                }
                                            } else {
                                                b.plan?.localPlanId = b.localPlanId
                                                b.plan?.localBlockId = b.localBlockId
                                                b.plan?.let {
                                                    AgsDB.updatePlan(it, false)
                                                }
                                            }
                                        }
                                        val isExitLocal =
                                            allBlocks.find { b.blockId == it.blockId || b.localBlockId == it.localBlockId }
                                        if (isExitLocal == null) allBlocks.add(b)
                                        else {
                                            for ((i, lb) in allBlocks.withIndex()) {//更新列表中的字段
                                                if (lb.blockId == b.blockId || lb.localBlockId == b.localBlockId) {
                                                    allBlocks[i].plan = b.plan
                                                    allBlocks[i].blockId = b.blockId
                                                    allBlocks[i].planId = b.planId
                                                    allBlocks[i].updateTime = b.updateTime
                                                    allBlocks[i].region = b.region
                                                    allBlocks[i].regionName = b.regionName
                                                    break
                                                }
                                            }
                                        }
                                    }
                                    showComplete(true, updateBlockList.toList()) //加载更新地块数据
                                }
                            }
                            index = maxIndex
                        }
                        LogFileHelper.log("load net block end")
                    }
                    allComplete(true, allBlocks.toList())
                    updateBlockState = false
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                LogFileHelper.log("local get nearby block err: $e")
                Log.e("zhy", "local get nearby block err: ${e.message}")
                allComplete(false, emptyList())
                showComplete(false, emptyList())
            } finally {
                updateBlockState = false
            }
        }
    }

    suspend fun getAllNearbyBlocks(
        w: Double,
        e: Double,
        s: Double,
        n: Double,
        from: Long,
        to: Long,
        blockType: Int?,
        batchSize: Int = 100,
        batchData: suspend (List<BlockPlan>) -> Unit,
        complete: suspend (List<BlockPlan>) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            val time = System.currentTimeMillis()
            val localBlocks = mutableListOf<BlockPlan>()
            var page = 0
            while (true) {
                val temp = AgsDB.getNearbyBlocksByPage(
                    w,
                    e,
                    s,
                    n,
                    from,
                    to,
                    batchSize,
                    page * batchSize,
                    blockType
                )
                batchData(temp)
                localBlocks.addAll(temp)
                if (temp.size < batchSize) break
                page++
            }
//            Log.d("zhy", "data base block: ${localBlocks.size}")
            //本地查到的地块
            Log.d(
                "zhy",
                "本地地块查询${localBlocks.size}条，共耗时${System.currentTimeMillis() - time}.ms "
            )
            complete(localBlocks)
        }
    }

    fun updatePlan(plan: Plan): Flow<Plan> {
        return flow {
            AgsDB.updatePlan(plan, true)
            emit(plan)
            AgsDB.sync()
        }.catch {
            Log.v("yuhang", "local update plan err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun updatePlan2(plan: Plan): Flow<Plan> {//不更新track
        return flow {
            AgsDB.updatePlan2(plan)
            emit(plan)
            AgsDB.sync()
        }.catch {
            Log.v("yuhang", "local update plan err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun savePlan(plan: Plan): Flow<Plan> {
        return flow {
            AgsDB.savePlan(plan)
            emit(plan)
            emitSync()
        }.catch {
            Log.v("yuhang", "local save plan err: $it")
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadSortieSim(sortie: Sortie, complete: (Sortie) -> Unit) {
        flow {
            AgsNet.uploadSortieSimulate(sortie)
            complete(sortie)
            emit(0)
            emitSync()
        }.catch {
            Log.d("shero", "uploadSortie err(sim):${it}")
            LogFileHelper.log("uploadSortie err(sim):${it}")
            complete(sortie)
            emit(0)
            emitSync()
        }
            .flowOn(Dispatchers.IO).collect {}
    }

    suspend fun uploadSortie(sortie: Sortie, complete: (Sortie) -> Unit) {
        flow {
            AgsDB.saveSortie(sortie)//保存架次并更新地块的作业信息
            addWorkArea(
                sortie.localBlockId,
                sortie.area.toDouble(),
                sortie.drug.toDouble(),
                SortieRoute(sortie.route),
                sortie.planPercent,
                sortie.additional,
                sortie.workPoints
            ) {
                val lp = AgsDB.getBlockPlan(sortie.localBlockId)
                DroneModel.blockPlan.emit(lp)
                if (lp == null) complete(sortie)
                else {
                    lp.let {
                        sortie.workName = lp.blockName
                        sortie.sprayPerMu = lp.plan?.param?.sprayMu?.toDouble() ?: 0.0
                        sortie.blockArea = lp.area.toDouble()
                        sortie.naviArea = lp.naviArea
                        sortie.workArea = it.workArea
                        sortie.workDrug = it.workDrug
                        if (lp.finish) sortie.unWorkArea = 0.0
                        else sortie.unWorkArea = lp.naviArea - sortie.workArea
                        complete(sortie)
                    }
                }
            }
            emit(0)
            emitSync()
        }.catch {
            Log.d("shero", "uploadSortie err:${it}")
            LogFileHelper.log("uploadSortie err:${it}")
        }.flowOn(Dispatchers.IO).collect {}
    }

    fun getPlan(localPlanId: Long): Flow<Plan?> {
        return flow {
            emit(AgsDB.getPlan(localPlanId))
        }.catch {
            Log.v("yuhang", "local get plan err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun getBlockPlan(localBlockId: Long): Flow<BlockPlan?> {
        return flow {
            emit(AgsDB.getBlockPlan(localBlockId))
        }.catch {
            Log.v("yuhang", "local get block plan err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun getSortieTrack(blockId: Long): Flow<SortieRoute?> {
        return flow {
            emit(AgsDB.getSortieLastTrack(blockId))
        }.catch {
            Log.v("yuhang", "local get sortie track err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun deleteBlock(localBlockId: Long): Flow<String?> {
        return flow {
            AgsDB.deleteBlock(localBlockId)
            emit(null)
//            emitSync()
        }.catch {
            Log.v("yuhang", "local delete local block err: $it")
        }.flowOn(Dispatchers.IO)
    }

    fun getBlock(blockId: Long): Flow<Block?> {
        return flow {
            emit(AgsDB.getBlockDetail(blockId))
        }.catch {
            Log.v("yuhang", "local get block err: $it")
        }.flowOn(Dispatchers.IO)
    }

    suspend fun workFinish(localBlockId: Long) {
        flow {
            emit(AgsDB.blockFinish(localBlockId))
        }.catch {
            Log.v("yuhang", "local get block err: $it")
        }.flowOn(Dispatchers.IO).collect()
    }

    suspend fun blockWorking(localBlockId: Long): Flow<Int> {
        return flow {
            AgsDB.blockWorking(localBlockId)
            emit(1)
        }.catch {
            Log.v("yuhang", "local block working err: $it")
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addWorkArea(
        localBlockId: Long,
        workArea: Double,
        workDrug: Double,
        sortieRoute: SortieRoute?,
        workPercent: Int,
        additional: SortieAdditional?,
        workPoints: List<GeoHelper.LatLngAlt>,
        complete: suspend () -> Unit = {},
    ) {
        flow {
            AgsDB.updateWorkArea(
                true,
                localBlockId,
                workArea,
                workDrug,
                workPercent,
                additional,
                sortieRoute,
                workPoints
            )
            complete()
            emit(1)
        }.catch {
            Log.v("yuhang", "local addWorkArea err: $it")
        }.flowOn(Dispatchers.IO).collect {}
    }

    class SyncData(id: Int)

    private fun emitSync() {
        EventBus.getDefault().post(SyncData(1))
    }

    //更新指定类型范围内所有地块
    suspend fun updateAllLocalBlocks(
        n: Double, s: Double, e: Double, w: Double,
        from: Long, to: Long, blockType: Int?,
        insertComplete: suspend (List<BlockPlan>) -> Unit,
        updateComplete: suspend (List<BlockPlan>) -> Unit,
        taskComplete: (Boolean) -> Unit = {},
    ) {
        withContext(Dispatchers.IO) {
            try {
                val localBlocks = mutableListOf<BlockPlan>()
                if (!updateBlockState) {
                    LogFileHelper.log("AgsNet updateAllLocalBlocks start.....")
//                    Log.d("zhy","AgsNet updateAllLocalBlocks start.....")
                    updateBlockState = true
                    val blocks =
                        AgsNet.nearbyBlocks(n, s, e, w, from, to, blockType?.toString() ?: "")
                    LogFileHelper.log("AgsNet nearby blocks: ${blocks.size}")
//                    Log.d("zhy","AgsNet nearby blocks: ${blocks.size}")
                    val step = 100
                    if (blocks.isNotEmpty()) {
                        var index = 0
                        while (index < blocks.size) {
                            val blockIds = arrayListOf<Long>()
                            val maxIndex =
                                if (index + step < blocks.size) index + step else blocks.size
                            for (i in index until maxIndex) {
                                blockIds.add(blocks[i].blockId)
                            }
                            LogFileHelper.log("load net block: from $from to $to")
//                            Log.d("zhy","load net block: from $from to $to")
                            val infos =
                                AgsNet.nearbyBlockInfo(blockIds.toTypedArray(), from, to)
                            if (infos.isNotEmpty()) {
                                val insertBlockList = mutableListOf<BlockPlan>()
                                val updateBlockList = mutableListOf<BlockPlan>()
                                for (b in infos) {
                                    val localBlock = AgsDB.getBlockIsExit(b.blockId)
                                    if (localBlock?.delete == 1 || localBlock?.edit == 1) continue
                                    if (localBlock == null) {//本地没有这块地
                                        insertBlockList.add(b)
                                    } else if (localBlock.working == 0 && ((b.updateTime == 0L || localBlock.updateTime < b.updateTime!!) || localBlock.regionName.isEmpty())) {//如果本地有这块地，且地块已经完成，并且地块没有删除，则更新本地地块
                                        b.localBlockId = localBlock._id
                                        b.localPlanId = localBlock.localPlanId
                                        updateBlockList.add(b)
                                    }
                                }
                                //新增的地块
                                if (insertBlockList.isNotEmpty()) {
                                    LogFileHelper.log("insertBlockList begin")
//                                    Log.d("zhy","insertBlockList begin")
                                    val insertBlockListIds =
                                        AgsDB.saveBlockPlans(insertBlockList)
                                    val plans = mutableListOf<Plan>()
                                    for ((i, b) in insertBlockList.withIndex()) {
                                        val localBlockId = insertBlockListIds[i]._id
                                        b.localBlockId = localBlockId
                                        if (b.planId != 0L) {//这个地块有plan
                                            val lp =
                                                AgsDB.getPlanByRemoteId(b.planId)//看本地有没有存过这个plan
                                            if (lp == null) {//本地没有存过这个plan
                                                if (b.plan != null) {
                                                    insertBlockListIds[i].also {
                                                        it.planId = b.plan!!.planId
                                                        it.planRoutes =
                                                            SortieRoute(b.plan!!.track)
                                                        it.naviArea = b.plan!!.naviArea
                                                        it.workDrug = 0.0
                                                        it.workArea = 0.0
                                                        it.working = 0
                                                        it.finish = 0
                                                        it.additional = null
                                                        it.workRoutes = null
                                                        it.region = b.region ?: 0
                                                        it.regionName = b.regionName ?: ""
                                                    }
                                                    plans.add(b.plan!!)
                                                }
                                            }
                                        }
                                    }
                                    val localPlans = AgsDB.savePlans(plans)
                                    for (p in localPlans) {
                                        insertBlockList.find { it.planId == p.planId }?.also {
                                            it.localPlanId = p._id
                                            it.plan?.localPlanId = p._id
                                        }
                                        insertBlockListIds.find { it.planId == p.planId }
                                            ?.also {
                                                it.localPlanId = p._id
                                            }
                                    }
                                    LogFileHelper.log("insertBlockList end")
//                                    Log.d("zhy","insertBlockList end")
                                }
                                //返回新增地块
                                insertComplete(insertBlockList.toList())
                                //更新的地块
//                                Log.d("zhy", "updateBlockList: ${updateBlockList.size}")
                                if (updateBlockList.isNotEmpty()) {
                                    LogFileHelper.log("updateBlockList begin")
//                                    Log.d("zhy","updateBlockList begin")
                                    AgsDB.updateBlockPlans(updateBlockList, 0)
                                    LogFileHelper.log("updateBlockList end")
//                                    Log.d("zhy","updateBlockList end")
                                    for (b in updateBlockList) {
                                        if (b.planId != 0L) {//这个地块有plan
                                            val lp =
                                                AgsDB.getPlanByRemoteId(b.planId)//看本地有没有存过这个plan
                                            if (lp == null) {//本地没有存过这个plan
                                                if (b.plan != null) {
                                                    b.plan?.localBlockId = b.localBlockId
                                                    val localPlanId =
                                                        AgsDB.savePlan(b.plan!!)//保存到本地
                                                    b.localPlanId =
                                                        localPlanId//更新要保存的blockplan的localPlanId
                                                    b.plan?.localPlanId = localPlanId
                                                }
                                            } else {
                                                b.plan?.localPlanId = b.localPlanId
                                                b.plan?.localBlockId = b.localBlockId
                                                b.plan?.let { AgsDB.updatePlan(it, false) }
                                            }
                                        }
                                        val isExitLocal =
                                            localBlocks.find { b.blockId == it.blockId }
                                        if (isExitLocal == null) localBlocks.add(b)
                                        else {
                                            for ((i, lb) in localBlocks.withIndex()) {//更新列表中的字段
                                                if (lb.blockId == b.blockId) {
                                                    localBlocks[i].plan = b.plan
                                                    localBlocks[i].blockId = b.blockId
                                                    localBlocks[i].planId = b.planId
                                                    localBlocks[i].updateTime = b.updateTime
                                                    localBlocks[i].region = b.region
                                                    localBlocks[i].regionName = b.regionName
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                                updateComplete(updateBlockList.toList())
                            }
                            index = maxIndex
                        }
                        LogFileHelper.log("load net block end")
//                        Log.d("zhy","load net block end")
                    }
                    updateBlockState = false
                } else {
                    Log.w("zhy", "updateAllLocalBlocks is running..............")
                    LogFileHelper.log("updateAllLocalBlocks is running..............")
                }
                LogFileHelper.log("AgsNet updateAllLocalBlocks end.....")
                taskComplete(true)
//                Log.d("zhy","AgsNet updateAllLocalBlocks end.....")
            } catch (e: Throwable) {
                e.printStackTrace()
                taskComplete(false)
                LogFileHelper.log("local get nearby block err: $e")
                Log.e("zhy", "local get nearby block err: ${e}")
            } finally {
                updateBlockState = false
            }

        }
    }

    suspend fun deleteBreakpoint(localBlockId: Long) {
        withContext(Dispatchers.IO) {
            AgsDB.deleteBreakpoint(localBlockId)
        }
    }

    suspend fun saveBreakpoint(localBlockId: Long, bk: VKAg.BreakPoint) {
        withContext(Dispatchers.IO) {
            val localBK = LocalBlockBreakpoint(
                localBlockId = localBlockId,
                breakpoint = bk.toString()
            )
            AgsDB.saveBreakpoint(localBK)
        }
    }

    suspend fun getBreakpoint(localBlockId: Long, complete: (VKAg.BreakPoint?) -> Unit) {
        withContext(Dispatchers.IO) {
            val localBK = AgsDB.getBreakpoint(localBlockId)
            if (localBK == null || localBK.breakpoint.isBlank()) {
                complete(null)
            } else {
                val bk = VKAg.BreakPoint.fromString(localBK.breakpoint)
                complete(bk)
            }
        }
    }
}