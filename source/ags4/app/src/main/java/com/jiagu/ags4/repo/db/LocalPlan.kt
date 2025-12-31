package com.jiagu.ags4.repo.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.utils.formatPlanParamInfo
import com.jiagu.ags4.utils.formatRoutePoint
import com.jiagu.ags4.utils.parsePlanParamInfo
import com.jiagu.ags4.utils.parseRoutePoint
import com.jiagu.device.model.RoutePoint
import java.util.Locale


//Created by gengmeng on 5/15/24.

class RoutePointConvert {
    @TypeConverter
    fun objectToString(b: List<RoutePoint>): String {
        return formatRoutePoint(b)
    }
    @TypeConverter
    fun stringToObject(s: String): List<RoutePoint> {
        return parseRoutePoint(s)
    }
}

class PlanParamConvert {
    @TypeConverter
    fun objectToString(p: PlanParamInfo?): String? {
        return if (p == null) null else formatPlanParamInfo(p)
    }
    @TypeConverter
    fun stringToObject(s: String?): PlanParamInfo? {
        return if (s == null) null else parsePlanParamInfo(s)
    }
}

@TypeConverters(RoutePointConvert::class, PlanParamConvert::class)
@Entity(tableName = "navi")
data class LocalPlan(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long,

    @ColumnInfo(name = "mode", defaultValue = "1") var routeMode: Int, // 航线模式
    @ColumnInfo(name = "track") var track: List<RoutePoint>, //轨迹
    @ColumnInfo(name = "width") var width: Float, //幅宽
    @ColumnInfo(name = "height") var height: Float, //高度
    @ColumnInfo(name = "speed") var speed: Float, //速度
    @ColumnInfo(name = "drug_qty") var drugQuantity: Float, //亩用量
    @ColumnInfo(name = "drug_fix") var drugFix: Int, //水泵百分比
    @ColumnInfo(name = "block_id") var blockId: Long,
    @ColumnInfo(name = "plan_id") var planId: Long,//默认等于0,等于0需要上传plan，上传规划后更新planId
    @ColumnInfo(name = "local_block_id") var localBlockId: Long,
    @ColumnInfo(name = "area") var naviArea: Double = 0.0, //规划面积
    //1-需要上传 0-不需要上传  默认等于0 不需要上传 更新规划后，把这个字段设为1 (同步时先同步未上传的，再同步未更新的)
    @ColumnInfo(name = "need_update") var needUpdate: Int = 0,
    @ColumnInfo(name = "plan_param") var planParam: PlanParamInfo?,//规划参数
) {
    fun toPlan(): Plan {
        val plan = Plan(routeMode, track,
            width, height, speed, drugQuantity, drugFix, blockId, planParam)
        plan.planId = planId
        plan.localPlanId = _id
        plan.blockId = blockId
        plan.localBlockId = localBlockId
        plan.naviArea = naviArea
        return plan
    }

    override fun toString(): String {
        return String.format(
            Locale.US, "LP: %d(%d),%d,%.2f,%.2f,%.2f,%.2f,%d," +
                "%d,%d,%s,%d,%.2f",
            _id, planId, routeMode, width, height, speed, drugQuantity, drugFix,
            blockId, localBlockId, planParam, needUpdate, naviArea)
    }
}

class PlanBrief(
    @ColumnInfo(name = "block_id") var blockId: Long,
    @ColumnInfo(name = "plan_id") var planId: Long
)

@Dao
interface LocalPlanDao {
    @Update(entity = LocalPlan::class)
    fun updatePlan(block: LocalPlan)
    @Query("SELECT * FROM navi")
    fun getPlans(): List<LocalPlan>

    @Query("SELECT * FROM navi WHERE plan_id = 0")
    fun getNoUploadedPlan(): List<LocalPlan>

    @Query("SELECT * FROM navi WHERE need_update = 1")
    fun getNoUpdatePlan(): List<LocalPlan>

    @Query("SELECT * FROM navi WHERE _id = :id")
    fun getPlanById(id: Long): LocalPlan?

    @Query("SELECT * FROM navi WHERE plan_id = :planId")
    fun getPlanByRemoteId(planId: Long): LocalPlan?

    @Query("SELECT block_id, plan_id FROM navi WHERE _id = :id")
    fun getPlanBriefById(id: Long): PlanBrief?

    @Query("SELECT * FROM navi WHERE plan_id = :planId")
    fun getPlanForRemoteId(planId: Long): LocalPlan

    @Query("SELECT * FROM navi WHERE local_block_id = :localBlockId")
    fun getAllPlanForBlockId(localBlockId: Long): List<LocalPlan>?

    @Query("SELECT * FROM navi WHERE _id > 0")
    fun getAllPlans(): List<LocalPlan>

    @Query("SELECT * FROM navi WHERE block_id = :blockId")
    fun getAllPlanForBlockRemoteId(blockId: Long): List<LocalPlan>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(block: LocalPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(block: List<LocalPlan>): List<Long>

    //上传过地块后，将是否更新的标记也置为false，既没上传，还更新了的plan只需要调一次上传的接口就可以了，不用再同步一次
    @Query("UPDATE navi SET block_id = :remoteBlockId, plan_id = :remotePlanId, need_update = 0 WHERE _id = :id")
    fun updatePlanRemoteId(id: Long, remoteBlockId: Long, remotePlanId: Long)

//    @Query("UPDATE navi SET width = :width, height = :height, speed = :speed, " +
//            "drug_qty = :drugQuantity, drug_fix = :drugFix, plan_param = :planParam, need_update = :needUpdate, track = :track WHERE _id = :id")
//    fun updatePlan(id: Long, width: Float, height: Float, speed: Float,
//                   drugQuantity: Float, drugFix: Int, planParam: PlanParam, needUpdate: Int, track: List<RoutePoint>)

    @Query("UPDATE navi SET need_update = :needUpdate WHERE _id = :id")
    fun updatePlanUpdateSignal(id: Long, needUpdate: Int)

    @Query("UPDATE navi SET plan_id = :time WHERE _id = :id")
    fun updatePlanCreateTime(id: Long, time: Long)

    @Query("DELETE FROM navi WHERE _id = :id")
    fun removePlan(id: Long)

    @Query("DELETE FROM navi")
    fun removeAll()
}