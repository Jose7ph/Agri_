package com.jiagu.ags4.repo.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.bean.SortieAdditional
import com.jiagu.ags4.bean.SortieRoute
import com.jiagu.ags4.bean.WorkRoute
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.Group
import com.jiagu.ags4.utils.logToFile
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.model.centerOfMapRing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AgsDB {
    private lateinit var db: AppDatabase
    var unsyncTime = -1L
    private val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `rental` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `droneId` TEXT NOT NULL, `sortieId` INTEGER NOT NULL, `area` REAL NOT NULL, `status` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL)")
        }
    }
    private val migration_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sortie ADD COLUMN lifting_weight REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE sortie ADD COLUMN lifting_distance REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE sortie ADD COLUMN battery TEXT")
        }
    }
    private val migration_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `no_fly_zone` (" +
                        "`nofly_id` INTEGER PRIMARY KEY NOT NULL," +
                        "`effect_start_time` INTEGER NOT NULL," +
                        "`orbit_str` TEXT NOT NULL," +
                        "`detail_address` TEXT NOT NULL," +
                        "`effect_end_time` INTEGER NOT NULL," +
                        "`orbit` TEXT NOT NULL," +
                        "`nofly_type` INTEGER NOT NULL," +
                        "`is_enable` INTEGER NOT NULL," +
                        "`effect_status` INTEGER NOT NULL," +
                        "`lat` REAL NOT NULL," +
                        "`lng` REAL NOT NULL" +
                        ")"
            )
            // 创建期望的唯一索引
            db.execSQL(
                """
                    CREATE UNIQUE INDEX index_no_fly_zone_nofly_id 
                    ON no_fly_zone (nofly_id ASC)
                    """
            )
            db.execSQL(
                """
                    CREATE INDEX index_no_fly_zone_nofly_type 
                    ON no_fly_zone (nofly_type ASC)
                    """
            )
            db.execSQL(
                """
                    CREATE INDEX index_no_fly_zone_detail_address 
                    ON no_fly_zone (detail_address ASC)
                    """
            )
            db.execSQL(
                """
                    CREATE INDEX index_no_fly_zone_lat 
                    ON no_fly_zone (lat ASC)
                    """
            )
            db.execSQL(
                """
                    CREATE INDEX index_no_fly_zone_lng 
                    ON no_fly_zone (lng ASC)
                    """
            )
        }
    }
    private val migration_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `block` ADD COLUMN `comment` TEXT DEFAULT '' NOT NULL")
        }
    }
    private val migration_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `block_breakpoint` (" +
                        "`local_block_id` INTEGER PRIMARY KEY NOT NULL," +
                        "`breakpoint` TEXT NOT NULL," +
                        "`create_time` INTEGER NOT NULL" +
                        ")"
            )
            // 添加唯一索引
            db.execSQL(
                """
            CREATE UNIQUE INDEX IF NOT EXISTS index_block_breakpoint_local_block_id 
            ON block_breakpoint(local_block_id)
            """
            )
            db.execSQL(
                """
            CREATE INDEX IF NOT EXISTS index_block_breakpoint_create_time 
            ON block_breakpoint(create_time)
            """
            )
        }
    }

    private val migration_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `block` ADD COLUMN `ext_data` INTEGER DEFAULT 0 NOT NULL")
        }
    }

    fun initialize(context: Context) {
        db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ags4")
            .addMigrations(migration_1_2).addMigrations(migration_2_3).addMigrations(migration_3_4)
            .addMigrations(migration_4_5).addMigrations(migration_5_6)
            .addMigrations(migration_6_7)
            .build()
    }

    fun saveBlockPlans(blocks: List<BlockPlan>): List<LocalBlock> {
        val dao = db.blockDao()
        val gson = Gson()
        val lbs = mutableListOf<LocalBlock>()
        for (block in blocks) {
            val centerLat = centerOfMapRing(block.boundary[0])
            val calib = block.calibPoints ?: doubleArrayOf()
            val lb = LocalBlock(
                0,
                block.blockType,
                block.blockName,
                block.area,
                block.boundary,
                gson.toJson(calib),
                block.blockId,
                AgsUser.userInfo?.userId ?: 0,
                DroneModel.groupId ?: 0
            )
            lb.lat = centerLat.latitude
            lb.lng = centerLat.longitude
            lb.region = block.region ?: 0
            lb.regionName = block.regionName ?: ""
            lb.createTime = block.createTime
            if (block.updateTime != null && block.updateTime != 0L) lb.updateTime =
                block.updateTime!!
            lb.workPercent = block.workPercent ?: 0
            lb.additional = block.additional
            lb.comment = block.comment
            lb.planId = block.planId
            lb.localPlanId = block.localPlanId
            lb.blockId = block.blockId
            lb.planRoutes = block.sortieRoute
            lb.uploaded = 1
            lb.edit = 0
            lb.hasExtData = block.hasExtData
            lbs.add(lb)
        }
        for ((i, id) in dao.insertList(lbs).withIndex()) {
            lbs[i]._id = id
        }
        return lbs
    }

    fun updateBlockPlans(blockPlans: List<BlockPlan>, isEdit: Int) {
        val dao = db.blockDao()
        val lbs = mutableListOf<LocalBlock>()
        for (blockPlan in blockPlans) {
            val lp = dao.getBlockDetailById(blockPlan.localBlockId) ?: continue
            val lb = blockPlan.toLocalBlock(blockPlan, lp, isEdit)
            lbs.add(lb)
        }
        dao.insertList(lbs)
    }

    fun BlockPlan.toLocalBlock(block: BlockPlan, localBlock: LocalBlock, isEdit: Int): LocalBlock {
        val gson = Gson()
        val centerLat = centerOfMapRing(block.boundary[0])
        val calib = block.calibPoints ?: doubleArrayOf()
        return localBlock.also {
            it.type = blockType
            it.blockName = blockName
            it.additional = additional
            it.workPercent = block.workPercent ?: 0
            it.planRoutes = block.sortieRoute
            it.area = area
            if (updateTime != null && updateTime != 0L) it.updateTime = updateTime!!
            it.boundary = block.boundary
            it.calibPoints = gson.toJson(calib)
            it.groupId = block.groupId
            it.lat = centerLat.latitude
            it.lng = centerLat.longitude
            it.edit = isEdit
            it.blockId = block.blockId
            it.planId = block.planId
            it.region = block.region ?: 0
            it.regionName = block.regionName ?: ""
            it.uploaded = 1
            it.edit = 0
            it.comment = block.comment
            it.hasExtData = block.hasExtData
        }
    }

    fun saveBlocks(blocks: List<Block>): List<Long> {
        val dao = db.blockDao()
        val gson = Gson()
        val localBlockIds = mutableListOf<Long>()
        val localBlocks = mutableListOf<LocalBlock>()
        for (block in blocks) {
            val shell = block.boundary[0]
            val center = centerOfMapRing(shell)
            val calib = block.calibPoints ?: doubleArrayOf()
            val lb = LocalBlock(
                0,
                block.blockType,
                block.blockName,
                block.area,
                block.boundary,
                gson.toJson(calib),
                block.localBlockId,
                AgsUser.userInfo?.userId ?: 0,
                DroneModel.groupId ?: 0
            )
            lb.lat = center.latitude
            lb.lng = center.longitude
            lb.createTime = block.createTime
            lb.updateTime = block.createTime
            lb.comment = block.comment
            localBlocks.add(lb)
        }
        localBlockIds.addAll(dao.insertList(localBlocks))
        return localBlockIds
    }

    fun getBlockDetail(id: Long): Block? {
        val dao = db.blockDao()
        return dao.getBlockDetailById(id)?.toBlock()
    }

    fun getBlockIsExit(blockId: Long): LocalBlock? {
        val dao = db.blockDao()
        return dao.getBlockIsExit(blockId)
    }

    fun getBlockPlan(id: Long): BlockPlan? {
        val dao = db.blockDao()
        val planDao = db.planDao()
        val bp = dao.getBlockDetailById(id)?.toBlockPlan()
        if (bp != null && bp.localPlanId != 0L) {//拿规划
            planDao.getPlanById(bp.localPlanId).let {
                bp.plan = it?.toPlan()
            }
        }
        return bp
    }

    fun updateBlock(block: Block, isClearPlan: Boolean) {
        val dao = db.blockDao()
        val gson = Gson()
        val calib = block.calibPoints ?: doubleArrayOf()
        val lp = dao.getBlockDetailById(block.localBlockId) ?: return
        val centerLat = centerOfMapRing(block.boundary[0])
        dao.updateBlock(lp.also {
            it.blockName = block.blockName
            it.area = block.area
            it.boundary = block.boundary
            it.calibPoints = gson.toJson(calib)
            it.lat = centerLat.latitude
            it.lng = centerLat.longitude
            it.updateTime = System.currentTimeMillis()
            it.edit = 1
            it.comment = block.comment
            if (isClearPlan) {
                //TODO 虽然本地删了，但是同步线上数据的时候又把线上规划同步到本地了
                it.planId = 0
                it.localPlanId = 0
                it.workRoutes = null
                it.finish = 0
                it.working = 0
            }
        })
    }

    fun getNearbyBlocksByPage(
        w: Double,
        e: Double,
        s: Double,
        n: Double,
        from: Long,
        to: Long,
        limit: Int,
        index: Int,
        blockType: Int?,
    ): List<BlockPlan> {
        logToFile("get nearby block userId:${AgsUser.userInfo?.userId}")
        val userId = AgsUser.userInfo?.userId ?: return listOf()
        val dao = db.blockDao()
        val planDao = db.planDao()
        val blocks = if (blockType != null) {
            dao.getBlocksByPage(userId, w, e, s, n, limit, index, blockType)
        } else {
            dao.getBlocksByPage(userId, w, e, s, n, limit, index)
        }
        logToFile("get nearby block size:${blocks.size}")
        val out = mutableListOf<BlockPlan>()
        for (block in blocks) {
            val bp = block.toBlockPlan()
            if (block.lastWorkTime < from || block.lastWorkTime > to) {
                bp.additional = null
                bp.workArea = 0.0
                bp.workDrug = 0.0
                bp.workPercent = 0
                bp.working = false
                bp.finish = false
            }
            if (bp.localPlanId != 0L) {//拿规划
                planDao.getPlanById(bp.localPlanId).let {
                    bp.plan = it?.toPlan()
                }
            }
            out.add(bp)
        }
        return out
    }

    fun getNearbyBlocksByPage(
        w: Double,
        e: Double,
        s: Double,
        n: Double,
        from: Long,
        to: Long,
        limit: Int,
        index: Int,
        blockType: Int?,
        blockName: String,
        blockState: Int,
        order: String,
    ): List<BlockPlan> {
        logToFile("new :get nearby block userId:${AgsUser.userInfo?.userId}")
        val userId = AgsUser.userInfo?.userId ?: return listOf()
        val dao = db.blockDao()
        val planDao = db.planDao()
        val blocks = if (blockType != null) {
            dao.getBlocksByPage(
                userId,
                w,
                e,
                s,
                n,
                limit,
                index,
                blockType,
                blockName,
                blockState,
                order
            )
        } else {
            dao.getBlocksByPage(userId, w, e, s, n, limit, index, blockName, blockState, order)
        }
        logToFile("new :get nearby block size:${blocks.size}")
        val out = mutableListOf<BlockPlan>()
        for (block in blocks) {
            val bp = block.toBlockPlan()
            if (block.lastWorkTime < from || block.lastWorkTime > to) {
                bp.additional = null
                bp.workArea = 0.0
                bp.workDrug = 0.0
                bp.workPercent = 0
                bp.working = false
                bp.finish = false
            }
            if (bp.localPlanId != 0L) {//拿规划
                planDao.getPlanById(bp.localPlanId).let {
                    bp.plan = it?.toPlan()
                }
            }
            out.add(bp)
        }
        return out
    }

    fun savePlan(plan: Plan): Long {
        val dao = db.planDao()
        val lp = LocalPlan(
            0,
            plan.routeMode,
            plan.track,
            plan.width,
            plan.height,
            plan.speed,
            plan.drugQuantity,
            plan.drugFix,
            plan.blockId,
            plan.planId,
            plan.localBlockId,
            plan.naviArea,
            0,
            plan.param
        )
        lp.needUpdate = 0
        lp.naviArea = plan.naviArea
        val localPlanId = dao.insert(lp)
        plan.localPlanId = localPlanId
        logToFile(
            "local save plan localPlanId:${localPlanId} planId:${plan.planId} localBlockId:${plan.localBlockId}/${plan.blockId} \n" + "plan.param:${plan.param?.toLog()}:${plan.param} 本地规划参数:${lp.planParam}"
        )

        val blockDao = db.blockDao()
        val lb = blockDao.getBlockDetailById(plan.localBlockId) ?: return localPlanId
        blockDao.updateBlock(lb.also {
            it.planId = plan.planId
            it.localPlanId = localPlanId
            it.planRoutes = SortieRoute(plan.track)
            it.naviArea = plan.naviArea
            it.workDrug = 0.0
            it.workArea = 0.0
            it.working = 0
            it.finish = 0
            it.additional = null
            it.workRoutes = null
        })
        return localPlanId
    }

    fun savePlans(plans: List<Plan>): List<LocalPlan> {
        val dao = db.planDao()
        val localPlans = mutableListOf<LocalPlan>()
        for (plan in plans) {
            val lp = LocalPlan(
                0,
                plan.routeMode,
                plan.track,
                plan.width,
                plan.height,
                plan.speed,
                plan.drugQuantity,
                plan.drugFix,
                plan.blockId,
                plan.planId,
                plan.localBlockId,
                plan.naviArea,
                0,
                plan.param
            )
            lp.needUpdate = 0
            lp.naviArea = plan.naviArea
            val localPlanId = dao.insert(lp)
            plan.localPlanId = localPlanId
            localPlans.add(lp)
        }

        for ((i, id) in dao.insertList(localPlans).withIndex()) {
            localPlans[i]._id = id
        }

        return localPlans
    }

    fun updatePlan(plan: Plan, needUpdate: Boolean) {
        val dao = db.planDao()
        val lp = dao.getPlanById(plan.localPlanId)
        if (lp != null) {
            dao.updatePlan(lp.also {
                it.width = plan.width
                it.height = plan.height
                it.speed = plan.speed
                it.drugQuantity = plan.drugQuantity
                it.drugFix = plan.drugFix
                it.planParam = plan.param
                it.needUpdate = if (needUpdate) 1 else 0
                it.track = plan.track
                it.naviArea = plan.naviArea
            })
        }
    }

    fun updatePlan2(plan: Plan) {//不更新track
        val dao = db.planDao()
        val lp = dao.getPlanById(plan.localPlanId)
        if (lp != null) {
            dao.updatePlan(lp.also {
                it.width = plan.width
                it.height = plan.height
                it.speed = plan.speed
                it.drugQuantity = plan.drugQuantity
                it.drugFix = plan.drugFix
                it.planParam = plan.param
                it.needUpdate = 1//本地更新规划，需要同步线上
                it.naviArea = plan.naviArea
            })
        }
    }

    fun getPlan(id: Long): Plan? {
        val dao = db.planDao()
        val plan = dao.getPlanById(id)
        return plan?.toPlan()
    }

    fun getPlanByRemoteId(planId: Long): Plan? {
        val dao = db.planDao()
        val plan = dao.getPlanByRemoteId(planId)
        return plan?.toPlan()
    }

    fun getSortieLastTrack(localBlockId: Long): SortieRoute? {
        val dao = db.blockDao()
        val gson = Gson()
        val routes = dao.getPlanRoute(localBlockId)
        return if (routes.isBlank()) null else gson.fromJson(routes, SortieRoute::class.java)
    }

    suspend fun saveSortie(sortie: Sortie) {
        val dao = db.sortieDao()
        val startTime = if (sortie.startTime > 0L) sortie.startTime
        else if (sortie.track.isNotEmpty()) sortie.track[0].timestamp
        else sortie.startTime
        val lp = LocalSortie(
            0,
            sortie.sortie,
            sortie.sprayWidth,
            sortie.cropType,
            sortie.drug,
            sortie.area,
            sortie.track,
            sortie.route,
            sortie.posData,
            sortie.localPlanId,
            sortie.localBlockId,
            sortie.planId,
            sortie.blockId ?: 0,
            sortie.additional,
            sortie.id,
            startTime,
            sortie.endTime,
            sortie.flightime,
            sortie.planPercent,
            (sortie.totalArea * 100).toInt(),
            sortie.lat0,
            sortie.lng0,
            sortie.groupId ?: 0,
            AgsUser.userInfo?.userId ?: 0,
            sortie.battery
        )
        lp.componentVersion = sortie.componentVersion
        lp.workMode = sortie.workMode
        lp.workType = sortie.workType
        val id = dao.insert(lp)
        logToFile("local save sortie: (${id}) lp.track:${lp.track?.size} sortie.track:${sortie.track.size} route:${lp.routes?.size} workPoint:${sortie.workPoints.size}")

        updateSyncTime()
    }

    fun updateWorkArea(
        isAdd: Boolean,
        localBlockId: Long,
        workArea: Double,
        workDrug: Double,
        workPercent: Int,
        additional: SortieAdditional?,
        sortieRoute: SortieRoute?,
        workPoints: List<GeoHelper.LatLngAlt>,
    ) {
        val blockDao = db.blockDao()
        val lb = blockDao.getBlockDetailById(localBlockId) ?: return
        if (lb.finish == 0) {//未完成，累加 更新percent and work area，否则不更新percent and work area
            blockDao.updateBlock(lb.also {
                if (isAdd) {
                    it.workArea += workArea
                    it.workDrug += workDrug
                    it.workPercent += workPercent
                } else {
                    it.workArea = workArea
                    it.workDrug = workDrug
                    it.workPercent = workPercent
                }
                it.lastWorkTime = System.currentTimeMillis()
                it.additional = additional
                it.planRoutes = sortieRoute
                if (it.workRoutes == null) {
                    it.workRoutes = WorkRoute(mutableListOf())
                    it.workRoutes!!.workRoute.add(workPoints)
                } else {
                    it.workRoutes!!.workRoute.add(workPoints)
                }
                logToFile("local update work area localBlockId:$localBlockId workArea:$workArea workDrug:$workDrug workPercent:$workPercent additional:$additional workPoint.size:${workPoints.size}")
            })
        }
    }

    fun blockFinish(localBlockId: Long) {
        val dao = db.blockDao()
        val lb = dao.getBlockDetailById(localBlockId) ?: return
        if (lb.finish == 0) {
            dao.updateBlock(lb.also {
                it.working = 0
                it.finish = 1
            })
        }
    }

    fun blockWorking(localBlockId: Long) {
        val dao = db.blockDao()
        dao.blockWorking(localBlockId)
    }

    fun deleteBlock(localBlockId: Long) {
        val dao = db.blockDao()

        val isUpload = dao.getIsUpload(localBlockId)
        if (isUpload == 0) dao.removeForLocalId(localBlockId)//没上传过的地块，直接删除
        else dao.setDeleteBlockStatus(localBlockId)//上传过的地块，将删除标记置为true
    }

    private var isSyncing = false
    fun sync(complete: (() -> Unit)? = null) {
//        Log.v("shero", "sync start isSyncing:$isSyncing netConnect:${DroneModel.netConnect} netIsConnect:${AgsUser.netIsConnect}")
        if (isSyncing) {//1正在同步 2网络可能显示连着，但是网络很差 3网络未连接
            complete?.invoke()
            return
        }
        GlobalScope.launch {
            if (!AgsUser.netIsConnect) {//网络断开，不进行地块，同步扣费数据
                isSyncing = false
                complete?.invoke()
                return@launch
            }
            isSyncing = true
            syncData()
            complete?.invoke()
        }
    }

    private suspend fun syncData(): Boolean {
        Log.v("shero", "sync data")
        return withContext(Dispatchers.IO) {
            try {
                syncBlocks()
                LogFileHelper.log("sync success")
                Log.v("shero", "sync success")
                getBlockPlan(DroneModel.localBlockId)?.let {
                    DroneModel.blockPlan.emit(it)
                }
                isSyncing = false
                true
            } catch (e: Throwable) {
                LogFileHelper.log("sync failed: $e")
                Log.v("shero", "sync failed: $e")
                getBlockPlan(DroneModel.localBlockId)?.let {
                    DroneModel.blockPlan.emit(it)
                }
                isSyncing = false
                false
            }
        }
    }

    private suspend fun syncBlocks() {
        val userId = AgsUser.userInfo?.userId ?: return

        val dao = db.blockDao()
        val notUpload = dao.getNoUploadedBlock(userId)//未上传的地块
        if (notUpload.isNotEmpty()) logToFile("sync blocks ${notUpload.size}")
        for (b in notUpload) {
            val block = b.toBlock()
            AgsNet.uploadBlockSync(block)
//            Log.v("shero", "upload block: (${block.blockId}) $block")
            dao.updateUploadStatus(b._id, block.blockId, b.createTime, b.updateTime)//上传true 编辑true
        }
        val editBlocks = dao.getIsEditBlock(userId)//修改的地块
        if (editBlocks.isNotEmpty()) logToFile("sync edit blocks ${editBlocks.size}")
        for (b in editBlocks) {
            val block = b.toBlock()
            AgsNet.updateBlocksSync(block)//编辑true
            dao.updateEditStatus(b._id)
        }
        syncPlan(dao)
    }

    private suspend fun syncPlan(blockDao: LocalBlockDao) {
        val dao = db.planDao()
        val plans = dao.getNoUploadedPlan()
        if (plans.isNotEmpty()) logToFile("sync upload plans(${plans})")
        for (p in plans) {
            val lb = blockDao.getBlockDetailById(p.localBlockId) ?: continue
            if (lb._id != 0L && lb.blockId == 0L) continue//地块没有上传时，不上传规划
            if (p.blockId == 0L) {
                p.blockId = lb.blockId
            }
            val plan = p.toPlan()
            AgsNet.uploadPlanSync(plan)
//            Log.v("shero", "sync planId:${p.planId}:${plan.planId} _id:${p._id} blockId:${p.blockId}")
            dao.updatePlanRemoteId(p._id, p.blockId, plan.planId)

            lb.let {//更新地块的planId和updateTime
                blockDao.updatePlanIdAndUpdateTime(lb._id, plan.planId, plan.updateTime)
            }
        }
        val updateLps = dao.getNoUpdatePlan()
        if (updateLps.isNotEmpty()) logToFile("sync update plans(${updateLps})")
        for (p in updateLps) {
            val plan = p.toPlan()
            AgsNet.updatePlanSync(plan)
            dao.updatePlanUpdateSignal(p._id, 0)//0不需要更新
            val lb = blockDao.getBlockDetailById(p.localBlockId) ?: continue
            blockDao.updateUpdateTime(lb._id, plan.updateTime)//更新地块的updateTime
        }
        syncSorties(dao)
    }

    private suspend fun syncSorties(planDao: LocalPlanDao) {
        val dao = db.sortieDao()
        var sid: Long = 0
        var uploadedSize = 0
        LogFileHelper.log("sync sorties start")
        val allSorties = dao.getAllSorties()
        LogFileHelper.log("sync sorties size: ${allSorties.size}")
        Log.v("shero", "allSorties(${allSorties.size})")
//        for (al in allSorties) {
//            Log.v("shero", "本地所有架次:${al} (${allSorties.size})")
//        }
        var sorties = dao.getSortiesById(sid)
        while (sorties.isNotEmpty()) {
            uploadedSize += sorties.size
            LogFileHelper.log("sync sorties userId: ${AgsUser.userInfo?.userId}(${sorties.size})")
//            logToFile("sync sorties userId: ${AgsUser.userInfo?.userId}(${sorties.size})")
            for (s in sorties) {
                if (s.planId == 0L) {
                    val pb = planDao.getPlanBriefById(s.localPlanId)//planId没有的话，去plan表里查一下
                    if (pb != null && s.localPlanId != 0L && pb.planId == 0L) continue//plan没有上传时，不上传架次
                    if (pb != null) {
                        s.blockId = pb.blockId
                        s.planId = pb.planId
                    }
                }
                val sortie = s.toSortie()
                //workType workMode liftingd liftingw
                LogFileHelper.log("upload sync sortie: ${sortie} localSortie:$s")
                if (s.uploaded == 0) {
                    try {
                        AgsNet.uploadSortieSync(null, sortie)
                    } catch (e: Throwable) {
                        LogFileHelper.log("upload sortie failed: $e")
                        continue
                    }

                    LogFileHelper.log("sync sorties success: ${sortie.id}/${sortie.sortie}/userId: ${sortie.userId}")
                    dao.removeSortie(s._id)
                }
            }
            sid = sorties.last()._id + 1
            sorties = dao.getSortiesById(sid)
        }
        LogFileHelper.log("sync sorties uploaded sortie size(${uploadedSize})")
//        logToFile("sync sorties uploaded sortie size(${uploadedSize})")

    }

    private fun updateSyncTime() {
        val dao = db.sortieDao()
        unsyncTime = dao.oldestUnsyncTime() ?: -1
    }

    fun insertParam(param: LocalParam): Long {
        val dao = db.paramDao()
        return dao.insert(param)
    }

    fun removeParam(id: Long) {
        val dao = db.paramDao()
        dao.removeParam(id)
    }

    fun getParamsByType(type: Int): List<LocalParam> {
        val dao = db.paramDao()
        return dao.getParamsByType(type, AgsUser.userInfo?.userId ?: 0)
    }

    fun getParamById(id: Long): LocalParam {
        val dao = db.paramDao()
        return dao.getParamById(id)
    }

    fun getParamByParamId(id: Long): LocalParam {
        val dao = db.paramDao()
        return dao.getParamByParamId(id)
    }

    fun removeRemoteParamsByType(type: Int) {
        val dao = db.paramDao()
        dao.deleteRemoteParamsByType(type, AgsUser.userInfo?.userId ?: 0)
    }

    fun updateParamById(
        id: Long,
        paramName: String,
        param: String,
        isLocal: Boolean,
        isUploaded: Boolean,
        isDelete: Boolean,
    ) {
        val dao = db.paramDao()
        dao.updateParamById(id, paramName, param, isLocal, isUploaded, isDelete)
    }

    fun removeAllNoFlyZone() {
        val dao = db.noFlyZoneDao()
        dao.removeAll()
    }

    fun saveNoFlyZones(noFlyZones: List<LocalNoFlyZone>) {
        val dao = db.noFlyZoneDao()
        dao.insertList(noFlyZones)
    }

    //同步禁飞区
    suspend fun syncNoFlyZone() {
        val pageSize = 1000
        val pageIndex = 1
        //有网
        if (AgsUser.netIsConnect) {
//                Log.d("zhy", "开始查询第${pageIndex}页")
            AgsNet.getNoflyList(pageIndex, pageSize, 0, "").networkFlow {
                Log.e("zhy", "获取禁飞区列表[${pageIndex}]失败,$it")
                LogFileHelper.log("获取禁飞区列表[${pageIndex}]失败,${it}")
            }.collectLatest {
                //当response正常响应后，先清空当前数据列表
                withContext(Dispatchers.IO) {
                    removeAllNoFlyZone()
                    //保存当前数据
                    val localDatas = it.list.map { data ->
                        data.toLocalData()
                    }
                    saveNoFlyZones(localDatas)
                    //判断是否还有下一页数据
                    if (it.total > it.list.size) {
                        //计算页数
                        val pageTotal = (it.total + pageSize - 1) / pageSize
                        var nextPage = pageIndex
                        repeat(pageTotal - 1) {
                            nextPage++
//                                Log.d("zhy", "开始查询第${nextPage}页")
                            AgsNet.getNoflyList(nextPage, pageSize, 0, "").networkFlow {
                                Log.e("zhy", "获取民用禁飞区列表[${nextPage}]失败,$it")
                                LogFileHelper.log("获取民用禁飞区列表[${nextPage}]失败,${it}")
                            }.collectLatest {
                                //保存当前数据
                                val nextDatas = it.list.map { data ->
                                    data.toLocalData()
                                }
                                saveNoFlyZones(nextDatas)
                            }
                        }
                    }
                    Log.d("zhy", "同步结束禁飞区。。。。。。 ")
                }
            }
        }
    }

    fun getNoFlyZoneList(
        w: Double,
        e: Double,
        s: Double,
        n: Double,
    ): List<LocalNoFlyZone> {
        val dao = db.noFlyZoneDao()
        return dao.getNoFlyZoneList(w, e, s, n)
    }

    fun saveGroupInfo(groups: List<Group>) {
        val dao = db.groupDao()
        dao.removeAll()
        val g = mutableListOf<LocalGroup>()
        groups.forEach { g.add(LocalGroup(it.groupId, it.groupName)) }
        dao.insertAll(g)
    }

    fun getGroups(): List<Group> {
        val dao = db.groupDao()
        val list = dao.getGroups()
        return list.map { localGroup ->
            Group(localGroup.groupId, localGroup.groupName)
        }
    }

    fun saveBreakpoint(blockBreakpoint: LocalBlockBreakpoint): Long {
        val dao = db.blockBreakpointDao()
        return dao.insert(blockBreakpoint)
    }

    fun deleteBreakpoint(localBlockId: Long) {
        val dao = db.blockBreakpointDao()
        dao.delete(localBlockId)
    }

    fun getBreakpoint(localBlockId: Long): LocalBlockBreakpoint? {
        val dao = db.blockBreakpointDao()
        return dao.getLocalBlockBreakpoint(localBlockId)
    }
//    fun removeAllBlocks() {
//        val dao = db.blockDao()
//        dao.removeAll()
//        val planDao = db.planDao()
//        planDao.removeAll()
//        val sortieDao = db.sortieDao()
//        sortieDao.removeAll()
//    }
}