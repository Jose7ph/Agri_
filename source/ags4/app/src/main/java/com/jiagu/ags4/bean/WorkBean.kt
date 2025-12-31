package com.jiagu.ags4.bean

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.repo.net.model.SortieInfo
import com.jiagu.ags4.utils.batteryToList
import com.jiagu.ags4.utils.formatMapBlock3D
import com.jiagu.ags4.utils.formatMapRing3D
import com.jiagu.ags4.utils.parseMapBlock3D
import com.jiagu.ags4.utils.parseMapRing3D
import com.jiagu.ags4.utils.posToListString
import com.jiagu.ags4.utils.routePointToArray
import com.jiagu.ags4.utils.trackNodeToList
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.PackageHelper
import com.jiagu.api.model.MapBlock3D
import com.jiagu.api.model.MapRing3D
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAg.POSData
import com.jiagu.tools.vm.RouteModel
import java.util.Arrays
import java.util.Locale

open class SortieItem(
    val droneId: String,   // 飞机ID
    val sortieId: Long,   // 架次ID
    val createTime: Long,   // 飞行开始时间
    val operUserName: String,   // 飞手姓名
    val isAuto: Boolean,   //  是否为自动
    val regionName: String,   // 地区名称
    val sprayRange: Float   // 喷洒面积)
) {
    val id: Long = 0  // system sortie id
}

class MeasurePole(var center: GeoHelper.LatLngAlt, var radius: Float) {
    fun stringify(): String {
        return String.format(
            Locale.US,
            "%f %f %f %f",
            center.longitude,
            center.latitude,
            center.altitude,
            radius
        )
    }

    companion object {
        fun fromString(str: String): MeasurePole {
            val ss = str.split(" ")
            return MeasurePole(
                GeoHelper.LatLngAlt(
                    ss[1].toDouble(),
                    ss[0].toDouble(),
                    ss[2].toDouble()
                ), ss[3].toFloat()
            )
        }
    }
}

// 地块编辑数据结构
class MeasureBlock(
    val boundary: MapRing3D,
    var holes: List<MapRing3D>,  //地块名称
    val poles: List<MeasurePole>,
    val calibPoints: MapRing3D,
) {
    fun stringify(): String {
        val sb = StringBuilder()
        sb.append(formatMapRing3D(boundary))
        sb.append('#')
        sb.append(formatMapBlock3D(holes))
        sb.append('#')
        sb.append(poles.joinToString(",") { it.stringify() })
        sb.append('#')
        sb.append(formatMapRing3D(calibPoints))
        return sb.toString()
    }

    companion object {
        fun fromString(str: String): MeasureBlock {
            val ss = str.split("#")
            val b = mutableListOf<GeoHelper.LatLngAlt>()
            if (ss[0].isNotBlank()) b.addAll(parseMapRing3D(ss[0]))
            val hole = mutableListOf<MapRing3D>()
            if (ss[1].isNotBlank()) hole.addAll(parseMapBlock3D(ss[1]))
            val p = mutableListOf<MeasurePole>()
            if (ss[2].isNotBlank()) ss[2].split(",").forEach { p.add(MeasurePole.fromString(it)) }
            val c = mutableListOf<GeoHelper.LatLngAlt>()
            if (ss[3].isNotBlank()) c.addAll(parseMapRing3D(ss[3]))
            return MeasureBlock(b, hole, p, c)
        }
    }
}

class WorkBlockInfo(
    val blockId: Long,
    val localBlockId: Long,
    val auxPoint: List<GeoHelper.LatLngAlt> = listOf()
)

//地块列表-分页(/blocks)
class Block(
    val blockType: Int,
    var blockName: String,  //地块名称
    val boundary: MapBlock3D,
    val calibPoints: DoubleArray,
    val area: Float,         //区域面积
    var groupId: Long?,
) {
    var localBlockId: Long = 0       //本地地块ID
    var blockNum = ""           //地块编码
    var createTime: Long = 0    //创建时间
    var updateTime: Long = 0    //修改时间
    var region: Int = 0         //区域编码
    var regionName = "NA"         //区域名称
    var userId: Long = 0        //账号ID
    var updateAuth: Boolean = false

    var blockId: Long = 0//后台blockId
    var localSaved = false

    var comment: String = ""//附加信息

    companion object {
        const val TYPE_BLOCK = 0
        const val TYPE_TRACK = 1
        const val TYPE_LIFTING = 2
        const val TYPE_AREA_CLEAN = 3
        const val TYPE_GROUND_TRACK = 4//小车自由航线
        const val TYPE_GROUND_BLOCK = 5//小车地块

        fun makeComment(yaw: Float): String {
            return String.format(Locale.US, "%.2f", yaw)
        }
    }
}

@Keep
class BlockPlan(
    var blockId: Long,
    val blockType: Int,
    var blockName: String,
    val boundary: MapBlock3D,
    val calibPoints: DoubleArray?,
    val area: Float,
    var createTime: Long,
    var workPercent: Int?,
    var planId: Long,
    val workId: Long,
    var additional: SortieAdditional?,
) {
    val barriers = mutableListOf<MapRing3D>()
    var localBlockId: Long = 0
    var localPlanId: Long = 0
    var regionName: String? = null
    var region: Int? = null

    var plan: Plan? = null
    var sortieRoute: SortieRoute? = null
    var workRoute: WorkRoute? = null
    var sortieRoute2: String = ""

    var updateTime: Long? = 0
    var groupId: Long = 0

    var workArea: Double = 0.0
    var workDrug: Double = 0.0
    var naviArea: Double = 0.0
    var finish: Boolean = false
    var working: Boolean = false

    var comment: String = ""//附加信息

    var hasExtData: Int = NOT_VAR_DATA//有没有变量数据 0-没有 1-有

    fun uniqueId(): String {
        return "$localBlockId-$blockId"
    }

    fun haveExtData(): Boolean {
        return hasExtData == HAS_VAR_DATA
    }

    private val name = "BP"
    override fun toString(): String {
        return String.format(
            Locale.US, "%-10s:" +
                    "%d(%d):%s,%d(%d),[%s]:%d," +
                    "%d,%.2f,%d,%d,%d,%d",
            name,
            localBlockId, blockId, blockName, localPlanId, planId, additional, workPercent,
            blockType, area, createTime, workId, updateTime, hasExtData
        )
    }

    companion object {
        const val HAS_VAR_DATA = 1
        const val NOT_VAR_DATA = 0
    }
}

class SortieRoute(val route: List<RoutePoint>?)
class WorkRoute(val workRoute: MutableList<List<GeoHelper.LatLngAlt>>) {
    override fun toString(): String {
//        Log.v("shero", "WorkRoute toString: $workRoute")
        val list = mutableListOf<String>()
        for (g in workRoute) {
            if (g.isEmpty()) continue
            val s = mutableListOf<String>()
            for (r in g) {
                s.add(String.format(Locale.US, "%f %f %f", r.latitude, r.longitude, r.altitude))
            }
            list.add(s.joinToString("/"))
        }
        val s = list.joinToString("|")
//        Log.v("shero", "WorkRoute toString s: $s")
        return s
    }

    companion object {
        fun fromString(s: String): WorkRoute? {
            if (s.isBlank()) return null
            try {
                val list = s.split("|")
                val out = mutableListOf<List<GeoHelper.LatLngAlt>>()
                for (t in list) {
                    val tt = t.split("/")
                    if (tt.isEmpty()) continue
                    val out2 = mutableListOf<GeoHelper.LatLngAlt>()
                    for (tt1 in tt) {
                        val tt2 = tt1.split(" ")
                        out2.add(GeoHelper.LatLngAlt(tt2[0].toDouble(), tt2[1].toDouble(), tt2[2].toDouble()))
                    }
                    out.add(out2)
                }
                return WorkRoute(out)
            } catch (e: Throwable) {
                Log.e("yuhang", "error work route: $e")
                LogFileHelper.log("error work route: $e")
                return null
            }
        }
    }
}

data class TrackNode(
    val timestamp: Long,
    val lat: Double,
    val lng: Double,
    val alt: Float,
    val height: Float,
    val hvel: Float,
    val vvel: Float,
    val yaw: Float,
    val pitch: Float,
    val roll: Float,
    val ftime: Int,
    val area: Float,
    val flow: Float,
    val dose: Float,
    val gps: Int,
    val locType: Int,
    val onAir: Boolean,
    val warning: Int,
    val pump: Int,
    val voltage: Float = 0f,  // 新增字段默认值
    val current: Float = 0f   // 新增字段默认值
) {
    override fun toString(): String {
        return buildString {
            // 基础字段
            append(String.format(
                Locale.US,
                "%d,%.7f,%.7f,%.2f,%.2f,%.2f,%.2f,%.1f,%.1f,%.1f,%d,%.2f,%.2f,%.2f,%d,%d,%d,%d,%d",
                timestamp, lat, lng, alt, height, hvel, vvel, yaw, pitch, roll,
                ftime, area, flow, dose, gps, locType, if (onAir) 1 else 0, warning, pump
            ))

            // 扩展字段（如果不是默认值则添加）
            if (voltage != 0f || current != 0f) {
                append(String.format(Locale.US, ",%.1f,%.1f", voltage, current))
            }
        }
    }

    companion object {
        fun fromString(s: String): TrackNode {
            val ss = s.split(",")
            return when (ss.size) {
                19 -> parseV1(ss)  // 老版本数据
                21 -> parseV2(ss)  // 新版本数据
                else -> throw IllegalArgumentException("Unsupported data format with ${ss.size} fields")
            }
        }

        private fun parseV1(ss: List<String>) = TrackNode(
            timestamp = ss[0].toLong(),
            lat = ss[1].toDouble(),
            lng = ss[2].toDouble(),
            alt = ss[3].toFloat(),
            height = ss[4].toFloat(),
            hvel = ss[5].toFloat(),
            vvel = ss[6].toFloat(),
            yaw = ss[7].toFloat(),
            pitch = ss[8].toFloat(),
            roll = ss[9].toFloat(),
            ftime = ss[10].toInt(),
            area = ss[11].toFloat(),
            flow = ss[12].toFloat(),
            dose = ss[13].toFloat(),
            gps = ss[14].toInt(),
            locType = ss[15].toInt(),
            onAir = ss[16].toInt() == 1,
            warning = ss[17].toInt(),
            pump = ss[18].toInt()
            // voltage 和 current 使用默认值 0f
        )

        private fun parseV2(ss: List<String>) = TrackNode(
            timestamp = ss[0].toLong(),
            lat = ss[1].toDouble(),
            lng = ss[2].toDouble(),
            alt = ss[3].toFloat(),
            height = ss[4].toFloat(),
            hvel = ss[5].toFloat(),
            vvel = ss[6].toFloat(),
            yaw = ss[7].toFloat(),
            pitch = ss[8].toFloat(),
            roll = ss[9].toFloat(),
            ftime = ss[10].toInt(),
            area = ss[11].toFloat(),
            flow = ss[12].toFloat(),
            dose = ss[13].toFloat(),
            gps = ss[14].toInt(),
            locType = ss[15].toInt(),
            onAir = ss[16].toInt() == 1,
            warning = ss[17].toInt(),
            pump = ss[18].toInt(),
            voltage = ss[19].toFloat(),
            current = ss[20].toFloat()
        )
    }
}

// "%d,%s,%d,%d,%d,%f,%f,%f,%f|%f|%f|..."
// timestamp, batId, cycle, status, percent, voltage, current, temperature,
// cell[0]|cell[1]|...
//电池数据
class Battery(
    val timestamp: Long,
    val batId: String,
    val cycle: Int,
    val status: Int,
    val percent: Int,
    val voltage: Float,
    val current: Float,
    val temperature: Float
) {
    override fun toString(): String {
        return String.format(
            Locale.US, "%d,%s,%d,%d,%d,%.2f,%.2f,%.1f",
            timestamp,
            batId, cycle, status, percent,
            voltage, current, temperature
        )
    }

    companion object {
        fun fromString(s: String): Battery {
            val ss = s.split(",")
            return Battery(
                ss[0].toLong(),
                ss[1],
                ss[2].toInt(), ss[3].toInt(), ss[4].toInt(),
                ss[5].toFloat(), ss[6].toFloat(), ss[7].toFloat()
            )
        }
    }
}

class Sortie(
    val id: String,  //飞机ID
    val sortie: Int, //架次ID
    val sprayWidth: Float,
    val cropType: Int,
    val drug: Float,  //药量
    val area: Float, //亩数
    val track: List<TrackNode>,

    var blockId: Long?,//线上blockId
    val groupId: Long?,
    val planPercent: Int,
    val flightime: Int,
    val totalArea: Double, //米
    var componentVersion: String
) {
    var planId: Long = 0//线上planId
    var additional: SortieAdditional? = null

    var localBlockId: Long = 0//本地blockId
    var localPlanId: Long = 0//本地planId

    var userId: Long = 0
    var startTime: Long = 0
    var endTime: Long = 0

    var posData: List<POSData>? = null
    var route: List<RoutePoint> = mutableListOf()
    var workPoints = mutableListOf<GeoHelper.LatLngAlt>()
    var lat0: Double = 0.0
    var lng0: Double = 0.0
    var supplement: SortieSupplement = SortieSupplement(0, "", "", "")
    var workType: Int = DroneModel.TYPE_SPRAY
    var workMode: Int = DroneModel.TYPE_MODE_MA
    var workName = "N/A"

    var workArea: Double = 0.0
    var workDrug: Double = 0.0
    var naviArea: Double = 0.0
    var blockArea: Double = 0.0
    var sprayPerMu: Double = 0.0
    var unWorkArea: Double = 0.0
    var battery: List<Battery> = listOf()

    private val name = "Sortie"
    override fun toString(): String {
        return String.format(
            Locale.US,
            "%-10s:" +
                    "%s,%d,%.2f,%d,%.2f,%.2f,%d," +
                    "blockId[%d],%d,%d,%d,%.2f,%s," +
                    "planId[%d],additional[%s],localBlockId[%d],localPlanId[%d],%d,%d,%d,%d,%d," +
                    "%s,%d,%d,%.2f,%d",
            name,
            id,
            sortie,
            sprayWidth,
            cropType,
            drug,
            area,
            track.size,
            blockId ?: 0,
            groupId ?: 0,
            planPercent,
            flightime,
            totalArea,
            componentVersion,
            planId,
            additional,
            localBlockId,
            localPlanId,
            userId,
            startTime,
            endTime,
            posData?.size ?: 0,
            route.size,
            supplement,
            workType,
            workMode,
            unWorkArea,
            battery.size
        )
    }

    fun toSortieInfo(): SortieInfo {
        return SortieInfo(
            id, sortie, sprayWidth, cropType, drug, area, trackNodeToList(track),
            blockId, groupId, planPercent, flightime, totalArea, componentVersion
        ).also {
            it.planId = planId
            it.additional = SortieAdditional.additionalToString(additional) ?: ""
            it.localBlockId = localBlockId
            it.localPlanId = localPlanId
            it.userId = userId
            it.startTime = startTime
            it.endTime = endTime
            it.posData = if (posData == null) listOf() else posToListString(posData!!)
            it.route = routePointToArray(route).toList()
            it.lat0 = lat0
            it.lng0 = lng0
            it.supplement = supplement.toString()
            it.workType = workType
            it.workMode = workMode
            it.battery = batteryToList(battery)
        }
    }

    fun getSupplement(context: Context): SortieSupplement {
        return SortieSupplement(
            PackageHelper.getAppVersionCode(context),
            PackageHelper.getAppVersionName(context),
            AgsUser.flavor,
            ControllerFactory.deviceModel
        )
    }
}

class SortieSupplement(
    val appVersionCode: Int,
    val appVersionName: String,
    val flavor: String,
    val deviceModel: String
) {
    //v1: "Supple:%d,%s,%s,%s", appVersionCode, appVersionName, flavor, deviceModel
    override fun toString(): String {
        return String.format(
            Locale.US,
            "%d,%s,%s,%s",
            appVersionCode,
            appVersionName,
            flavor,
            deviceModel
        )
    }
}

class SortieAdditional(
    val bk: VKAg.BreakPoint?
) {
    //v1: 只包含BreakPoint
    override fun toString(): String {
        return String.format(Locale.US, "%s", bk.toString())
    }

    companion object {

        fun additionalToString(s: SortieAdditional?): String? {
            return if (s?.bk == null) null else String.format(Locale.US, "%s", s.bk.toString())

        }

        fun fromString(s: String): SortieAdditional {
            return SortieAdditional(VKAg.BreakPoint.fromString(s))
        }
    }
}

class Plan(
    var routeMode: Int,
    var track: List<RoutePoint>,
    var width: Float,
    var height: Float,
    var speed: Float,
    var drugQuantity: Float,
    var drugFix: Int,
    var blockId: Long,
    var param: PlanParamInfo?
) {
    var planId = 0L

    var localPlanId: Long = 0
    var localBlockId: Long = 0//本地id

    var graphId: String? = null

    var naviArea: Double = 0.0

    var updateTime: Long = 0

    private val name = "Plan"

    override fun toString(): String {
        return String.format(
            Locale.US, "%-10s:" +
                    "%d,%.2f,%.2f,%.2f,%.2f,%d,%s," +
                    "%d(%d),%d(%d),%s,%s,%d",
            name,
            routeMode, width, height, speed, drugQuantity, drugFix, param?.toLog() ?: "",
            localBlockId, blockId, localPlanId, planId, graphId ?: "", track.toString(), updateTime
        )
    }
}

@Keep
class  PlanParamInfo {
    var curEdge: Int = 0//当前边
    var curRidge: Float = 3.5f//当前垄距
    var curAngle: Int = 0//当前角度
    var curRadius: Float = 10f//抛投覆盖半径
    var planType: Int = RouteModel.PLAN_BLOCK//规划类型
    var barrierSafeDist: Float = 5f//障碍物安全距离
    var edgeSafeDist: Array<Float> = arrayOf()//所有边安全距离

    //新增字段
    var sprayMu: Float = 1000f//亩用量（ml/亩）
    var seedMu: Float = 1000f//播撒用量（kg/亩）
    var centrifugalSize: Int = 50//喷头转速（%）
    var seedRotateSpeed: Int = 50//甩盘转速（%）
    var valveSize: Int = 50//阀门开度%
    var pumpSize: Float = 5f//流量大小 L/min = 水泵大小%
    var pumpMode: Int = 0//流量水泵模式
    var height: Float = 2f//高度 / 相对作物高度
    var speed: Float = 5f//速度
    var maxSpeed: Float = 10f//最大速度
    var materialId: Long = 0
    var materialName: String = ""

    //新增字段
    var seedMode: Int = 0//流量播撒模式
    var repeatCount: Int = 0//航线重复次数

    fun setMPlanType(t: Int) {
        planType = t
    }

    override fun toString(): String {//越南 土耳其 葡萄牙 中小数点用逗号表示
        return String.format(
            Locale.US, "P2:%d,%.2f,%d,%.2f,%d,%.2f," +
                    "%s," +
                    "%.2f,%.2f,%d,%d,%d,%.2f,%d," +
                    "%.2f,%.2f,%.2f,%d,%s,%d,%d",
            curEdge, curRidge, curAngle, curRadius, planType, barrierSafeDist,
            edgeSafeDist.joinToString("/") { String.format(Locale.US, "%.2f", it) },
            sprayMu, seedMu, centrifugalSize, seedRotateSpeed, valveSize, pumpSize, pumpMode,
            height, speed, maxSpeed, materialId, materialName, seedMode, repeatCount
        )
    }

    fun toLog(): String {
        return "" +
                "当前边:${curEdge} " +
                "当前垄距:${curRidge} " +
                "当前角度:${curAngle} " +
                "当前半径:${curRadius} " +
                "规划类型:${planType} " +
                "障碍物安全距离:${barrierSafeDist} " +
                "收边距离:${Arrays.toString(edgeSafeDist)} " +
                "亩用量 喷洒:${sprayMu} " +
                "亩用量 播撒:${seedMu} " +
                "轮盘转速:${seedRotateSpeed} " +
                "离心喷头:${centrifugalSize} " +
                "阀门大小:${valveSize} " +
                "水泵大小:${pumpSize} " +
                "作业模式 喷洒:${pumpMode} " +
                "作业模式 播撒:${seedMode} " +
                "高度:${height} " +
                "速度:${speed} "+
                "重复次数:${repeatCount} "
    }

    companion object {
        private val name = "P2:"
        fun fromString(s: String): PlanParamInfo {
            val planParam = PlanParamInfo()
            try {
                if (s.contains("P:")) {
                    val ps = s.split(":")
                    val list = ps[1].split(",")
                    val isNew = list.size == 21
                    planParam.curEdge = list[0].toInt()
                    planParam.curRidge = list[1].toFloat()
                    planParam.curAngle = list[2].toInt()
                    planParam.curRadius = list[3].toFloat()
                    planParam.setMPlanType(list[4].toInt())
//                    planParam.planType = list[4].toInt()
                    planParam.barrierSafeDist = list[5].toFloat()
                    planParam.edgeSafeDist = list[6].split("/").map {
                        try {
                            it.toFloat()
                        } catch (e: Exception) {
                            0f
                        }
                    }.toTypedArray()
                    planParam.sprayMu = list[7].toFloat()
                    planParam.seedMu = list[8].toFloat()
                    planParam.centrifugalSize = list[9].toInt()
                    planParam.seedRotateSpeed = list[10].toInt()
                    planParam.valveSize = list[11].toInt()
                    planParam.pumpSize = list[12].toFloat()
                    planParam.pumpMode = list[13].toInt()
                    planParam.height = list[14].toFloat()

                    planParam.speed = if (isNew) list[15].toFloat() else list[16].toFloat()
                    planParam.maxSpeed = if (isNew) list[16].toFloat() else list[17].toFloat()
                    planParam.materialId = if (isNew) list[17].toLong() else list[18].toLong()
                    planParam.materialName = if (isNew) list[18] else list[19]
                    if (isNew) {
                        if (list.size > 19) planParam.seedMode = list[19].toInt()
                    } else {
                        if (list.size > 20) planParam.seedMode = list[20].toInt()
                    }
                    planParam.repeatCount =if (isNew) list[20].toInt() else list[21].toInt()
                    return planParam
                } else if (s.contains(name)) {
                    val ps = s.split(":")
                    val list = ps[1].split(",")
                    planParam.curEdge = list[0].toInt()
                    planParam.curRidge = list[1].toFloat()
                    planParam.curAngle = list[2].toInt()
                    planParam.curRadius = list[3].toFloat()
                    planParam.setMPlanType(list[4].toInt())
//                    planParam.planType = list[4].toInt()
                    planParam.barrierSafeDist = list[5].toFloat()
                    planParam.edgeSafeDist = list[6].split("/").map {
                        try {
                            it.toFloat()
                        } catch (e: Exception) {
                            0f
                        }
                    }.toTypedArray()
                    planParam.sprayMu = list[7].toFloat()
                    planParam.seedMu = list[8].toFloat()
                    planParam.centrifugalSize = list[9].toInt()
                    planParam.seedRotateSpeed = list[10].toInt()
                    planParam.valveSize = list[11].toInt()
                    planParam.pumpSize = list[12].toFloat()
                    planParam.pumpMode = list[13].toInt()
                    planParam.height = list[14].toFloat()

                    planParam.speed = list[15].toFloat()
                    planParam.maxSpeed = list[16].toFloat()
                    planParam.materialId = list[17].toLong()
                    planParam.materialName = list[18]
                    planParam.seedMode = list[19].toInt()
                    if(list.size>20){
                        planParam.repeatCount = list[20].toInt()

                    }
                    return planParam
                } else if (s.contains("{")) {
                    return Gson().fromJson(s, PlanParamInfo::class.java)
                } else {
                    return planParam
                }
            } catch (e: Exception) {
                Log.v("shero", "解析规划参数报错2: $e")
                return planParam
            }
        }
    }
}

@Keep
class TemplateData {
    var sprayMu: Float = 0f//亩用量（L/亩）
    var seedMu: Float = 0f//播撒用量（kg/亩）
    var centrifugalSize: Int = 0//喷头转速（%）
    var seedRotateSpeed: Int = 0//甩盘转速（%）
    var valveSize: Int = 0//阀门开度%
    var pumpSize: Float = 0f//流量大小 L/min = 水泵大小%
    var pumpMode: Int = 0//流量水泵模式
    var height: Float = 0f//高度
    var width: Float = 0f//宽度
    var speed: Float = 0f//速度
    var maxSpeed: Float = 0f//最大速度
    var materialId: Long = 0
    var materialName: String = ""
    var autoSpeed: Float = 0f
    var seedMode: Int = 0//流量播撒模式

    override fun toString(): String {
        return String.format(
            Locale.US,
//            "%-10s:" +
            "%.2f,%.2f,%.2f," +
                    "%d,%d,%d,%d," +
                    "%.2f,%.2f,%.2f,%.2f," +
                    "%d,%s,%.2f,%d",
//                name,
            sprayMu, seedMu, pumpSize,
            centrifugalSize, seedRotateSpeed, valveSize, pumpMode,
            height, width, speed, maxSpeed,
            materialId, materialName, autoSpeed, seedMode
        )
    }

    companion object {
        private val name = "Template"
        fun fromString(s: String): TemplateData {
            val data = s.split(",")
            val tem = TemplateData()
            tem.sprayMu = data[0].toFloat()
            tem.seedMu = data[1].toFloat()
            tem.pumpSize = data[2].toFloat()
            tem.centrifugalSize = data[3].toInt()
            tem.seedRotateSpeed = data[4].toInt()
            tem.valveSize = data[5].toInt()
            tem.pumpMode = data[6].toInt()
            tem.height = data[7].toFloat()
            tem.width = data[8].toFloat()
            tem.speed = data[9].toFloat()
            tem.maxSpeed = data[10].toFloat()
            tem.materialId = data[11].toLong()
            tem.materialName = data[12]
            if (data.size >= 14) tem.autoSpeed = data[13].toFloat()
            if (data.size >= 15) tem.seedMode = data[14].toInt()
            return tem
        }
    }
}

@Keep
class DroneState(
    val droneId: String,
    val sortie: Long,
    val totalArea: Float,
    val live: Int,
    val info: String
)

@Keep
data class DroneInfo(
    val lng: Double,
    val lat: Double,
    val mha: Float,
    val flowSpeed: Float,
    val xspeed: Float,
    val height: Float,
//    val mileage: Float,
    val recordTime: Long
)

@Keep
data class FlyHistoryLocusWarper(val sortie: FlyHistoryLocus)

@Keep
data class FlyHistoryLocus(
    val droneId: String,
    val sortieId: Long,
    val droneInfos: List<DroneInfo>,
    val imgIds: Map<Long, String>,
    val region: Int,
    val regionName: Int,
    val blockId: Long,
    val timeLength: Long,
    val blockNum: String,
    val startTime: Long,
    val endTime: Long,
    val mileage: Int,
    val sprayCapacity: Float, // 喷洒药量
    val sprayRange: Float, // 喷洒面积
    val userId: Long,
    val workId: Long,
    val isAuto: Boolean,
    val groupId: Long,
    val sprayWidth: Float,
    val boundary: Array<DoubleArray>,
    val posData: List<String>?
)

@Keep
class Locus(lat: Double, lng: Double, val angle: Float, val time: Long) : GeoHelper.LatLng(lat, lng)

@Keep
class SprayLocus(val pump: Boolean, val locus: MutableList<Locus>)

class TftzTrack(val lng: Double, val lat: Double, val height: Double?)
class TrackBrief(val sortieId: Long, val droneId: String, val droneInfos: List<TftzTrack>?)

@Keep
class CallBody(val code: Int, val msg: String)//code=200成功 不等于200失败

class TemplateParam(val id: Long, val name: String, var param: TemplateData?) {
    var localId: Long = 0
}