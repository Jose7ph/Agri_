package com.jiagu.ags4.repo.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.SortieAdditional
import com.jiagu.ags4.bean.SortieRoute
import com.jiagu.ags4.bean.WorkRoute
import com.jiagu.ags4.utils.formatMapBlock3D
import com.jiagu.ags4.utils.parseMapBlock3D
import com.jiagu.ags4.utils.parseSortieAdditional
import com.jiagu.ags4.utils.parseSortieRoute
import com.jiagu.ags4.utils.sortieAdditionalToString
import com.jiagu.ags4.utils.sortieRouteToString
import com.jiagu.ags4.utils.stringToWorkRoute
import com.jiagu.ags4.utils.workRouteToString
import com.jiagu.api.model.MapBlock3D
import com.jiagu.api.model.MapRing3D
import kotlin.math.roundToInt

class BoundaryConvert {
    @TypeConverter
    fun objectToString(b: MapBlock3D): String {
        return formatMapBlock3D(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): MapBlock3D {
        return if (s == null) listOf() else parseMapBlock3D(s)
    }
}

class BlockAdditionalConvert {
    @TypeConverter
    fun objectToString(b: SortieAdditional?): String? {
        return sortieAdditionalToString(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): SortieAdditional? {
        return parseSortieAdditional(s)
    }
}

class BlockSortieRouteConvert {
    @TypeConverter
    fun objectToString(b: SortieRoute?): String {
        return sortieRouteToString(b)
    }

    @TypeConverter
    fun stringToObject(s: String): SortieRoute? {
        return parseSortieRoute(s)
    }
}

class BlockWorkRouteConvert {
    @TypeConverter
    fun objectToString(b: WorkRoute?): String? {
        return workRouteToString(b)
    }

    @TypeConverter
    fun stringToObject(s: String): WorkRoute? {
        return stringToWorkRoute(s)
    }
}

@TypeConverters(
    BoundaryConvert::class,
    BlockAdditionalConvert::class,
    BlockSortieRouteConvert::class,
    BlockWorkRouteConvert::class
)
@Entity(tableName = "block", indices = [Index(value = ["lat"]), Index(value = ["lng"])])
data class LocalBlock(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long,
    @ColumnInfo(name = "type") var type: Int, //测绘工具
    @ColumnInfo(name = "block_name") var blockName: String, //地块名称
    @ColumnInfo(name = "area") var area: Float,//地块面积
    @ColumnInfo(name = "boundary") var boundary: MapBlock3D,//边界点
    @ColumnInfo(name = "calib_point") var calibPoints: String,//校准点
    @ColumnInfo(name = "block_id") var blockId: Long,
    @ColumnInfo(name = "user_id") var userId: Long,
    @ColumnInfo(name = "group_id") var groupId: Long,
) {
    //中心点
    @ColumnInfo(name = "lat")
    var lat: Double = 0.0

    @ColumnInfo(name = "lng")
    var lng: Double = 0.0

    @ColumnInfo(name = "create_time")
    var createTime: Long = System.currentTimeMillis()

    @ColumnInfo(name = "update_time")
    var updateTime: Long = createTime

    @ColumnInfo(name = "navi_area")
    var naviArea: Double = 0.0

    @ColumnInfo(name = "work_area")
    var workArea: Double = 0.0

    @ColumnInfo(name = "work_drug")
    var workDrug: Double = 0.0

    @ColumnInfo(name = "work_percent")
    var workPercent: Int = 0

    @ColumnInfo(name = "additional")
    var additional: SortieAdditional? = null

    @ColumnInfo(name = "plan_routes")
    var planRoutes: SortieRoute? = null//replan track

    @ColumnInfo(name = "work_routes")
    var workRoutes: WorkRoute? = null//已作业航线

    @ColumnInfo(name = "uploaded")
    var uploaded: Int = 0//1代表上传成功 0代表没有上传成功

    @ColumnInfo(name = "edit")
    var edit: Int = 0//1代表修改了 0代表没有修改

    @ColumnInfo(name = "local_plan_id")
    var localPlanId: Long = 0//本地planId

    @ColumnInfo(name = "plan_id")
    var planId: Long = 0//网上planId

    @ColumnInfo(name = "finish")
    var finish: Int = 0//0-未完成 1-已完成

    @ColumnInfo(name = "working")
    var working: Int = 0//1-进行中

    @ColumnInfo(name = "is_delete")
    var delete: Int = 0//1-已删除

    @ColumnInfo(name = "last_work_time")
    var lastWorkTime: Long = System.currentTimeMillis()//最后一次作业的时间，超过这个时间的断点和作业数据都清除，保存作业面积的时间更新一下

    @ColumnInfo(name = "region")
    var region: Int = 0

    @ColumnInfo(name = "region_name")
    var regionName: String = ""

    @ColumnInfo(name = "comment")
    var comment: String = ""

    @ColumnInfo(name = "ext_data")
    var hasExtData: Int = BlockPlan.NOT_VAR_DATA
    fun toBlock(): Block {
        val gson = Gson()
        val name = if (blockName.isBlank()) "NONAME" else blockName
        val b = Block(
            type, name, boundary,
            gson.fromJson(calibPoints, DoubleArray::class.java), area, groupId
        )
        b.localBlockId = _id
        b.createTime = createTime
        b.blockId = blockId
        b.region = region
        b.regionName = regionName
        b.comment = comment
        b.updateTime = updateTime
        return b
    }

    fun toBlockPlan(): BlockPlan {
        val gson = Gson()
        val barriers = mutableListOf<MapRing3D>()
        for (i in 1 until boundary.size) {
            barriers.add(boundary[i])
        }
        val bp = BlockPlan(
            blockId, type, blockName, boundary,
            gson.fromJson(calibPoints, DoubleArray::class.java), area, createTime,
            workPercent, planId, 0, additional
        )
        bp.localPlanId = localPlanId
        bp.localBlockId = _id
        bp.blockId = blockId
        bp.region = region
        bp.regionName = regionName
        bp.finish = finish == 1
        bp.working = working == 1
        bp.naviArea = naviArea
        bp.workArea = workArea
        bp.workDrug = workDrug
        bp.comment = comment
        bp.additional = if (finish == 1) null else additional
        bp.sortieRoute = planRoutes
        bp.barriers.addAll(barriers)
        bp.workRoute = workRoutes
        bp.workPercent = if (naviArea == 0.0) 0 else if (finish == 1) 100 else (workArea / naviArea * 100).roundToInt()
        bp.updateTime = updateTime
        bp.hasExtData = hasExtData
        return bp
    }
}

class BlockBrief(
    @ColumnInfo(name = "block_id") var blockId: Long,
    @ColumnInfo(name = "update_time") var updateTime: Long
)

@Dao
interface LocalBlockDao {
    @Update(entity = LocalBlock::class)
    fun updateBlock(block: LocalBlock)
//    @Query("UPDATE block " +
//            "SET type = :type, block_name = :blockName, area = :area, boundary = :boundary, calib_point = :calibPoints, " +
//            "block_id = :blockId, user_id = :userId, group_id = :groupId, " +
//            "lat = :lat, lng = :lng, create_time = :createTime, update_time = :updateTime, " +
//            "work_area = :workArea, work_drug = :workDrug, work_percent = :workPercent, additional = :additional, plan_routes = :planRoutes, " +
//            "uploaded = :isUpload, edit = :isEdit, local_plan_id = :localPlanId, plan_id = :planId, region = :region, region_name = :regionName WHERE _id = :id")
//    fun updateBlockPlan(id: Long, type: Int, blockName: String, area: Float, boundary: MapBlock3D, calibPoints: String,
//                        blockId: Long, userId: Long, groupId: Long,
//                        lat: Double, lng: Double, createTime: Long, updateTime: Long,
//                        workArea: Double, workDrug: Double, workPercent: Int, additional: SortieAdditional?, planRoutes: SortieRoute?,
//                        isUpload: Int, isEdit: Int, localPlanId: Long, planId: Long, region: Int, regionName: String)

    @Query("SELECT * FROM block WHERE user_id = :userId AND lat > :s AND lat < :n AND lng > :w AND lng < :e AND is_delete = 0")
    fun getBlocks(userId: Long, w: Double, e: Double, s: Double, n: Double): List<LocalBlock>

    @Query(
        """
            SELECT * FROM block 
            WHERE user_id = :userId 
            AND lat > :s 
            AND lat < :n 
            AND lng > :w 
            AND lng < :e 
            AND is_delete = 0
            AND CASE :blockState
                WHEN 1 THEN working = 1
                WHEN 2 THEN finish = 1
                ELSE 1 = 1
                END
            AND (:blockName = '' OR block_name LIKE ('%' || :blockName || '%'))
            ORDER BY 
             CASE WHEN :order = 'ASC' THEN create_time END ASC,
             CASE WHEN :order = 'DESC' THEN create_time END DESC,
             CASE WHEN :order = '' THEN NULL END
            LIMIT :limit OFFSET :index
        """
    )
    fun getBlocksByPage(
        userId: Long,
        w: Double,
        e: Double,
        s: Double,
        n: Double,
        limit: Int,
        index: Int,
        blockName: String,
        blockState:Int,
        order: String
    ): List<LocalBlock>

    @Query(
        """
            SELECT * FROM block 
            WHERE user_id = :userId 
            AND type = :blockType 
            AND lat > :s 
            AND lat < :n 
            AND lng > :w 
            AND lng < :e 
            AND is_delete = 0 
            AND CASE :blockState
                WHEN 1 THEN working = 1
                WHEN 2 THEN finish = 1
                ELSE 1 = 1
                END
            AND (:blockName = '' OR block_name LIKE ('%' || :blockName || '%'))
            ORDER BY 
                CASE WHEN :order = 'ASC' THEN create_time END ASC,
                CASE WHEN :order = 'DESC' THEN create_time END DESC,
                CASE WHEN :order = '' THEN NULL END
            limit :limit offset :index
    """
    )
    fun getBlocksByPage(
        userId: Long,
        w: Double,
        e: Double,
        s: Double,
        n: Double,
        limit: Int,
        index: Int,
        blockType: Int,
        blockName: String,
        blockState:Int,
        order: String
    ): List<LocalBlock>

    @Query("SELECT * FROM block WHERE user_id = :userId AND lat > :s AND lat < :n AND lng > :w AND lng < :e AND is_delete = 0 limit :limit offset :index")
    fun getBlocksByPage(userId: Long, w: Double, e: Double, s: Double, n: Double, limit: Int, index: Int): List<LocalBlock>

    @Query("SELECT * FROM block WHERE user_id = :userId AND type = :blockType AND lat > :s AND lat < :n AND lng > :w AND lng < :e AND is_delete = 0 limit :limit offset :index")
    fun getBlocksByPage(userId: Long, w: Double, e: Double, s: Double, n: Double, limit: Int, index: Int,blockType:Int): List<LocalBlock>

//    @Query("SELECT * FROM block WHERE lat > :s AND lat < :n AND lng > :w AND lng < :e")
//    fun getNearByBlocks(w: Double, e: Double, s: Double, n: Double): List<LocalBlock>

//    @Query("SELECT block_id, update_time FROM block")
//    fun getBlocks(): List<BlockBrief>

    @Query("SELECT * FROM block")
    fun getAllBlocks(): List<LocalBlock>

    @Query("SELECT * FROM block WHERE user_id = :userId AND is_delete = 0")
    fun getAllBlocks(userId: Long): List<LocalBlock>

    @Query("SELECT block_id, update_time FROM block WHERE block_id IN (:blockId) AND is_delete = 0")
    fun getBlockByRemoteId(blockId: List<Long>): List<BlockBrief>

    @Query("SELECT block_id FROM block WHERE _id = :id AND is_delete = 0")
    fun getRemoteBlockId(id: Long): Long

    @Query("SELECT uploaded FROM block WHERE _id = :id AND is_delete = 0")
    fun getIsUpload(id: Long): Int

    @Query("SELECT * FROM block WHERE block_id = :blockId AND is_delete = 0")
    fun getBlockByRemoteId(blockId: Long): LocalBlock?

    @Query("DELETE FROM block WHERE block_id = :remoteBlockId")
    fun removeForRemoteId(remoteBlockId: Long)

    @Query("DELETE FROM block")
    fun removeAll()

    @Query("DELETE FROM block WHERE _id = :blockId")
    fun removeForLocalId(blockId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(block: LocalBlock): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(blocks: List<LocalBlock>): List<Long>

//    @Query("UPDATE block SET block_name = :blockName, area = :area, boundary = :boundary, calib_point = :calibPoints," +
//            "lat = :lat, lng = :lng, update_time = :updateTime, plan_id = 0, plan_id = 0, plan_routes = :planRoutes, edit = 1 WHERE _id = :id AND is_delete = 0")
//    fun updateBlock(id: Long, blockName: String, area: Float, boundary: MapBlock3D, calibPoints: String, lat: Double, lng: Double, updateTime: Long, planRoutes: SortieRoute? = null)
    //修改地块后，断点清空，规划清空

    @Query("UPDATE block SET block_id = :remoteId, uploaded = 1 WHERE _id = :id AND is_delete = 0")
    fun updateBlockIdByLocalId(id: Long, remoteId: Long)

    @Query("SELECT * FROM block WHERE uploaded = 0 AND user_id = :userId AND is_delete = 0")
    fun getNoUploadedBlock(userId: Long): List<LocalBlock>

    @Query("SELECT * FROM block WHERE edit = 1 AND user_id = :userId AND is_delete = 0")
    fun getIsEditBlock(userId: Long): List<LocalBlock>

    @Query("UPDATE block SET block_id = :remoteId, create_time = :createTime, update_time = :updateTime, uploaded = 1, edit = 0 WHERE _id = :id AND is_delete = 0")
    fun updateUploadStatus(id: Long, remoteId: Long, createTime: Long, updateTime: Long)

    @Query("UPDATE block SET edit = 0 WHERE _id = :id")
    fun updateEditStatus(id: Long)

//    @Query("UPDATE block SET finish = 1, work_percent = 100, additional = '', " +
//            "work_area = work_area + :workArea, work_drug = work_drug + :workDrug, " +
//            "plan_routes = :planRoutes WHERE _id = :id")
//    fun blockFinish(id: Long, workArea: Double, workDrug: Double, planRoutes: SortieRoute?)

    @Query("UPDATE block SET working = 1, finish = 0 WHERE _id = :id")
    fun blockWorking(id: Long)

//    @Query("UPDATE block SET additional = :additional, work_percent = :percent, plan_routes = :planRoutes, last_work_time = :lastWorkTime WHERE _id = :id")
//    fun updateAdditional(id: Long, additional: String, percent: Int, planRoutes: SortieRoute?, lastWorkTime: Long = System.currentTimeMillis())

    @Query("UPDATE block SET working = 0, finish = 1, work_percent = 100, additional = :additional WHERE _id = :id")
    fun blockFinish(
        id: Long,
        additional: String = ""
    )//作业完成后要清空断点，因为在上传架次更新作业面积的时候，已完成的地块不会更新，不清断点的话，下次拿到的还是上一次的断点

//    @Query("UPDATE block SET work_area = work_area + :workArea, work_drug = work_drug + :workDrug, " +
//            "work_percent = :workPercent, additional = :additional, plan_routes = :planRoutes WHERE _id = :id AND is_delete = 0")
//    fun updatePercentAdd(id: Long, workArea: Double, workDrug: Double, workPercent: Int, additional: String, planRoutes: SortieRoute?)

//    @Query("UPDATE block SET work_area = :workArea, work_drug = :workDrug, work_percent = :workPercent, " +
//            "additional = :additional, plan_routes = :planRoutes WHERE _id = :id AND is_delete = 0")
//    fun updatePercent(id: Long, workArea: Double, workDrug: Double, workPercent: Int, additional: String, planRoutes: SortieRoute)

//    @Query("UPDATE block SET plan_id = :planId, navi_area = :naviArea, plan_routes = :planRoutes, " +
//            "finish = 0, working = 0, work_percent = -1, work_area = 0, work_drug = 0, additional = :additional WHERE _id = :id AND is_delete = 0")
//    fun updateUploadPlanId(id: Long, planId: Long, naviArea: Double, planRoutes: SortieRoute?, additional: SortieAdditional?)//重新开始

    @Query("UPDATE block SET plan_id = :planId, navi_area = :naviArea WHERE _id = :id AND is_delete = 0")
    fun updatePlanId(id: Long, planId: Long, naviArea: Double)//重新开始

    @Query("SELECT * FROM block WHERE _id = :id AND is_delete = 0")
    fun getBlockDetailById(id: Long): LocalBlock?

    @Query("SELECT * FROM block WHERE block_id = :blockId")
    fun getBlockAvailable(blockId: Long): LocalBlock?

    @Query("SELECT finish FROM block WHERE _id = :id AND is_delete = 0")
    fun getBlockFinish(id: Long): Int

    @Query("SELECT plan_routes FROM block WHERE _id = :localBlockId AND is_delete = 0")
    fun getPlanRoute(localBlockId: Long): String

    @Query("UPDATE block SET is_delete = 1 WHERE _id = :id")
    fun setDeleteBlockStatus(id: Long)

    @Query("SELECT * FROM block WHERE block_id = :blockId")
    fun getBlockIsExit(blockId: Long): LocalBlock?

    @Query("UPDATE block SET plan_id = :planId, update_time = :updateTime WHERE _id = :id AND is_delete = 0")
    fun updatePlanIdAndUpdateTime(id: Long, planId: Long, updateTime: Long)

    @Query("UPDATE block SET update_time = :updateTime WHERE _id = :id AND is_delete = 0")
    fun updateUpdateTime(id: Long, updateTime: Long)
}