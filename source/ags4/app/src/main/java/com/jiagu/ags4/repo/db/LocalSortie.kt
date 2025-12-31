package com.jiagu.ags4.repo.db

import android.util.Log
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
import com.jiagu.ags4.bean.Battery
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.bean.SortieAdditional
import com.jiagu.ags4.bean.TrackNode
import com.jiagu.ags4.utils.batteryToArray
import com.jiagu.ags4.utils.formatRoutePoint
import com.jiagu.ags4.utils.parseBattery
import com.jiagu.ags4.utils.parsePosData
import com.jiagu.ags4.utils.parseRoutePoint
import com.jiagu.ags4.utils.parseSortieAdditional
import com.jiagu.ags4.utils.parseTrackNode
import com.jiagu.ags4.utils.posToString
import com.jiagu.ags4.utils.sortieAdditionalToString
import com.jiagu.ags4.utils.trackNodeToArray
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.vkprotocol.VKAg
import java.util.Locale


//Created by gengmeng on 5/16/24.

class SortieRoutePointConvert {
    @TypeConverter
    fun objectToString(b: List<RoutePoint>?): String? {
        return if (b.isNullOrEmpty()) null else formatRoutePoint(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): List<RoutePoint>? {
        return if (s.isNullOrBlank()) null else parseRoutePoint(s)
    }
}

class SortieTrackConvert {
    @TypeConverter
    fun objectToString(b: List<TrackNode>?): String? {
        return trackNodeToArray(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): List<TrackNode>? {
        return parseTrackNode(s)
    }
}
class SortieBatteryConvert {
    @TypeConverter
    fun objectToString(b: List<Battery>?): String? {
        return batteryToArray(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): List<Battery>? {
        return parseBattery(s)
    }
}

class SortiePosConvert {
    @TypeConverter
    fun objectToString(b: List<VKAg.POSData>?): String? {
        return posToString(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): List<VKAg.POSData> {
        return parsePosData(s)
    }
}

class SortieAdditionalConvert {
    @TypeConverter
    fun objectToString(b: SortieAdditional?): String? {
        return if (b == null) null else sortieAdditionalToString(b)
    }

    @TypeConverter
    fun stringToObject(s: String?): SortieAdditional? {
        return parseSortieAdditional(s)
    }
}

@TypeConverters(
    SortieRoutePointConvert::class,
    SortieTrackConvert::class,
    SortiePosConvert::class,
    SortieAdditionalConvert::class,
    SortieBatteryConvert::class
)
@Entity(tableName = "sortie", indices = [Index(value = ["drone_id", "sortie"], unique = true)])
data class LocalSortie(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long,

    @ColumnInfo(name = "sortie") var sortieId: Int,
    @ColumnInfo(name = "sprayWidth") var sprayWidth: Float,//幅宽
    @ColumnInfo(name = "crop") var crop: Int,//作物类型
    @ColumnInfo(name = "drug") var drug: Float, //用药量
    @ColumnInfo(name = "work_area") var workArea: Float,//作业面积
    @ColumnInfo(name = "track") var track: List<TrackNode>?,//track
    @ColumnInfo(name = "routes") var routes: List<RoutePoint>?,//replan track
    @ColumnInfo(name = "pos") var pos: List<VKAg.POSData>?,//pos track
    @ColumnInfo(name = "local_plan_id") var localPlanId: Long,
    @ColumnInfo(name = "local_block_id") var localBlockId: Long,
    @ColumnInfo(name = "plan_id") var planId: Long,
    @ColumnInfo(name = "block_id") var blockId: Long,
    @ColumnInfo(name = "additional") var additional: SortieAdditional?,//
    @ColumnInfo(name = "drone_id") var droneId: String,//
    @ColumnInfo(name = "start_time") var startTime: Long,//开始时间
    @ColumnInfo(name = "end_time") var endTime: Long,//开始时间
    @ColumnInfo(name = "duration") var duration: Int,
    @ColumnInfo(name = "percent") var percent: Int,
    @ColumnInfo(name = "odometer") var totalArea: Int,
    @ColumnInfo(name = "lat0") var lat0: Double,
    @ColumnInfo(name = "lng0") var lng0: Double,
    @ColumnInfo(name = "group_id") var groupId: Long,
    @ColumnInfo(name = "user_id") var userId: Long, //飞手
    @ColumnInfo(name = "battery") var battery: List<Battery>?
) {
    @ColumnInfo(name = "uploaded")
    var uploaded: Int = 0
    @ColumnInfo(name = "uploaded_vendor")
    var uploadedVendor: Int = 1
    @ColumnInfo(name = "work_type")
    var workType: Int = DroneModel.TYPE_SPRAY//喷洒
    @ColumnInfo(name = "work_mode")
    var workMode: Int = DroneModel.TYPE_MODE_MA//手动
    @ColumnInfo(name = "component_version")
    var componentVersion: String = ""//固件版本号
    @ColumnInfo(name = "lifting_weight")
    var liftingWeight: Double = 0.0//吊运重量
    @ColumnInfo(name = "lifting_distance")
    var liftingDistance: Double = 0.0//吊运距离

    fun toSortie(): Sortie {
        val sortie = Sortie(
            droneId, sortieId, sprayWidth, crop, drug, workArea, track ?: listOf(),
            blockId, groupId, percent, duration, totalArea / 100.0, componentVersion
        )
        sortie.planId = planId
        sortie.additional = additional
        sortie.localBlockId = localBlockId
        sortie.localPlanId = localPlanId
        sortie.userId = userId
        sortie.posData = pos
        sortie.startTime = startTime
        sortie.endTime = endTime
        sortie.lat0 = lat0
        sortie.lng0 = lng0
        sortie.route = routes ?: mutableListOf()
        sortie.workMode = workMode
        sortie.workType = workType
        sortie.battery = battery ?: listOf()
        return sortie
    }

    override fun toString(): String {
        return String.format(
            Locale.US,
            "LS:%d,%d,%.2f,%d,%.2f,%.2f," +
                    "%d,%d,%d,[%s],%s," +
                    "%d,%d,%d,%d,%d,%d,%d,%d,%d," +
                    "%d,%d,%s,%.1f,%.1f",
            _id,
            sortieId,
            sprayWidth,
            crop,
            drug,
            workArea,
            localPlanId,
            planId,
            blockId,
            additional,
            droneId,
            startTime,
            endTime,
            duration,
            percent,
            totalArea,
            groupId,
            userId,
            uploaded,
            uploadedVendor,
            workType,
            workMode,
            componentVersion,
            liftingWeight,
            liftingDistance
        )
    }
}

@Dao
interface LocalSortieDao {
    @Query("SELECT * FROM sortie")
    fun getAllSorties(): List<LocalSortie>

    @Query("SELECT * FROM sortie WHERE _id >= :id limit 10")
    fun getSortiesById(id: Long): List<LocalSortie>

    @Query("SELECT * FROM sortie WHERE local_plan_id = :localPlanId")
    fun getSortieForPlanId(localPlanId: Long): List<LocalSortie>

    @Query("UPDATE sortie SET uploaded = :uploaded WHERE _id = :id")
    fun updateSortieUploaded(id: Long, uploaded: Int)

    @Query("UPDATE sortie SET uploaded_vendor = :uploaded WHERE _id = :id")
    fun updateSortieUploadedVendor(id: Long, uploaded: Int)

    @Query("SELECT * FROM sortie WHERE local_block_id = :localBlockId")
    fun getSortieForBlockId(localBlockId: Long): List<LocalSortie>

    @Query("SELECT * FROM sortie WHERE block_id = :remoteBlockId OR local_block_id = :localBlockId")
    fun getSortieForRemoteAndBlockId(remoteBlockId: Long, localBlockId: Long): List<LocalSortie>

    @Query("SELECT * FROM sortie WHERE local_plan_id = :localPlanId AND local_block_id = :localBlockId")
    fun getSortieForPlanIdBlockId(localPlanId: Long, localBlockId: Long): List<LocalSortie>

    @Query("DELETE FROM sortie WHERE _id = :id")
    fun removeSortie(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(block: LocalSortie): Long

    @Query("SELECT MIN(start_time) FROM sortie")
    fun oldestUnsyncTime(): Long?

    @Query("DELETE FROM sortie")
    fun removeAll()
}