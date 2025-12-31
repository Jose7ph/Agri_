package com.jiagu.ags4.repo.net.model

import androidx.annotation.IntDef
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.bean.SortieItem
import com.jiagu.ags4.bean.UserStatistic
import com.jiagu.ags4.repo.db.LocalNoFlyZone
import com.jiagu.ags4.utils.arrayToMapBlock3D
import com.jiagu.ags4.utils.arrayToRoutePoint
import com.jiagu.ags4.utils.mapBlock3DToArray
import com.jiagu.ags4.utils.parsePlanParamInfo
import com.jiagu.ags4.utils.parseSortieAdditional
import com.jiagu.ags4.utils.posToListString
import com.jiagu.ags4.utils.routePointToListString
import com.jiagu.ags4.utils.sortieAdditionalToString
import com.jiagu.ags4.utils.sortieRouteInfoToSortieRoute
import com.jiagu.ags4.utils.trackNodeToList
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapRing3D
import com.jiagu.api.model.centerOfMapRing
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.tools.vm.RouteModel
import java.io.Serializable
import java.util.Locale

@Keep
open class Page<T>(val total: Int) {
    var pages: Int = 0

    // list有可能为null
    var list: List<T> = listOf()
        get() {
            if (field == null) return listOf()
            else return field
        }
}

@Keep
class RegisterInfo(
    val phoneNum: String,
    val email: String,
    val password: String,
    val verifyCode: String,
    val contactName: String
) {
    val accountType = 4 //账户类型3制造4运营
    val operatorType = 2 //团队类型1公司2个人
    val commitWay = 2
}

@Keep
class LoginInfo(val phoneNum: String, val password: String? = null, val verifyCode: String? = null)

@Keep
class RefreshInfo(val refreshToken: String)

@Keep
class SquareBlock(val blockIds: Array<Long>, val startTime: Long, val endTime: Long)

@Keep
class BlockItem(
    val blockType: Int,
    var blockName: String,  //地块名称
    val area: Float,         //区域面积
    val blockNum: String = "",           //地块编码
    val createTime: Long = 0,    //创建时间
    val regionName: String? = "NA",         //区域名称
    val blockId: Long = 0//后台blockId
)

@Keep
class BlockInfo(
    val blockType: Int, var blockName: String,  //地块名称
    val boundary: Array<DoubleArray>, val calibPoints: DoubleArray, val area: Float,         //区域面积
    var groupId: Long?, val altitude: Array<DoubleArray>? = null
) {
    var blockNum: String = ""
    var createTime: Long = 0    //创建时间
    var updateTime: Long = 0    //修改时间
    var region: Int = 0         //区域编码
    var regionName = "NA"         //区域名称
    var userId: Long = 0        //账号ID

    var blockId: Long = 0//后台blockId

    var comment: String = ""//附加信息

    fun toBlock(): Block {
        return Block(
            blockType, blockName, arrayToMapBlock3D(boundary, altitude), calibPoints, area, groupId
        ).also {
            it.createTime = createTime
            it.updateTime = updateTime
            it.region = region
            it.regionName = regionName
            it.userId = userId
            it.blockId = blockId
            it.blockNum = blockNum
            it.comment = comment
        }
    }
}

fun Block.toBlockInfo(): BlockInfo {
    val (boundary, altitude) = mapBlock3DToArray(this.boundary)
    return BlockInfo(
        this.blockType,
        this.blockName,
        boundary,
        calibPoints,
        this.area,
        groupId,
        altitude
    ).also {
        it.blockId = blockId
        it.createTime = createTime
        it.updateTime = updateTime
        it.region = region
        it.regionName = regionName
        it.userId = userId
        it.blockNum = blockNum
        it.comment = comment
    }
}

@Keep
class BlockPlanBrief(
    var blockId: Long,
    var updateTime: Long?,
) {
    fun toBlockPlan(): BlockPlan {
        return BlockPlan(
            blockId,
            0,
            "",
            listOf(),
            doubleArrayOf(),
            0f,
            0,
            0,
            0,
            0,
            null
        ).also {
            it.updateTime = updateTime ?: 0L
        }
    }
}

@Keep
class BlockPlanInfo(
    var blockId: Long,
    val blockType: Int,
    var blockName: String,
    val boundary: Array<DoubleArray>,
    val calibPoints: DoubleArray?,
    val area: Float,
    var createTime: Long,
    var workPercent: Int?,
    var planId: Long,
    val workId: Long?,
    var additional: String?,
    val altitude: Array<DoubleArray>? = null
) {
    var regionName: String? = null
    var region: Int? = null

    var plan: PlanInfo? = null
    var sortieRoute: SortieRouteInfo? = null

    var updateTime: Long? = 0
    var groupId: Long? = 0

    var comment: String? = ""//附加信息

    val hasExtData: Int = BlockPlan.NOT_VAR_DATA

    fun toBlockPlan(): BlockPlan {
        val boundarys = arrayToMapBlock3D(boundary, altitude)
        val barriers = mutableListOf<MapRing3D>()
        for (i in 1 until boundarys.size) {
            barriers.add(boundarys[i])
        }
        return BlockPlan(
            blockId,
            blockType,
            blockName,
            boundarys,
            calibPoints ?: doubleArrayOf(),
            area,
            createTime,
            workPercent,
            planId,
            workId ?: 0,
            parseSortieAdditional(additional ?: "")
        ).also {
            it.regionName = regionName
            it.region = region
            it.plan = plan?.toPlan()
            it.sortieRoute = sortieRouteInfoToSortieRoute(sortieRoute)
            it.groupId = groupId ?: 0
            it.barriers.addAll(barriers)
            it.comment = comment ?: ""
            it.updateTime = updateTime ?: 0L
            it.hasExtData = hasExtData
//            it.updateTime = updateTime
//            it.workArea = workArea
//            it.workDrug = workDrug
//            it.naviArea = naviArea
//            it.finish = finish
//            it.working = working
        }
    }
}

@Keep
class SortieRouteInfo(val route: List<String>?)

@Keep
class ChangePassword(val oldPassword: String, val newPassword: String)

@Keep
class ChangeHeader(val avatar: String)

@Keep
data class UploadResult(val url: String)

class DeviceFCU(val fcNumber: String)

class TeamName(val name: String)

class RegisterDrone(val data: Array<String>)

class RTKPay(val availableDays: Int)

class TrackData(val e: String, val t: Array<String>)

class ResetPasswordCode(
    val phoneNum: String,
    val password: String,
    val verifyCode: String
)

class AddTeam(val name: String)
class AddMembers(val userIds: Array<Long>)

@Keep
class BindInfo(val phoneEmail: String, val type: Int, val verifyCode: String) {
    companion object {
        const val TYPE_PHONE = 1
        const val TYPE_EMAIL = 2
    }
}

class VerifyCodeInfo {
    // 1=手机登录，2=账户注册，3=更改密码，4=提交资料，5=创建用户，8=绑定手机/邮箱 2=校验注册 3=重置密码
    enum class VerifyCodeTypeEnum(val codeType: Int) {
        MOBILE_LOGIN(1), ACCOUNT_REGISTER(2), CHANGE_PASSWORD(3), COMMIT_INFO(4), CREATE_MEMBER(5), CHANGE_ACCOUNT_FIRST(
            6
        ),
        CHANGE_ACCOUNT_SECOND(7), BIND_PHONE_EMAIL(8), CHECK_REGISTER(2), RESET_PASSWORD(3),
    }
}

@Keep
class PlanInfo(
    val routeMode: Int,
    var track: Array<String>,
    var width: Float,
    var height: Float,
    var speed: Float,
    var drugQuantity: Float,
    var drugFix: Int,
    var blockId: Long,
    var param: String?
) {
    var planId = 0L

    var localPlanId: Long = 0
    var localBlockId: Long = 0//本地id

    var graphId: String? = null

    var naviArea: Double = 0.0
    var updateTime: Long = 0

    fun vk2PlanType(vk: Int): Int {
        return when (vk) {
            VKAg.MISSION_UTYPE -> RouteModel.PLAN_BLOCK
            VKAg.MISSION_EDGE -> RouteModel.PLAN_EDGE
            VKAg.MISSION_LINE -> RouteModel.PLAN_TREE
            VKAg.MISSION_THROW -> RouteModel.PLAN_POLE
            VKAg.MISSION_UTYPE_EDGE -> RouteModel.PLAN_BLOCK_EDGE
            else -> RouteModel.PLAN_BLOCK
        }
    }

    fun toPlan(): Plan {
        val plan = Plan(
            routeMode, arrayToRoutePoint(track), width, height, speed, drugQuantity, drugFix,
            blockId, parsePlanParamInfo(param ?: "")
        ).also {
            it.planId = planId
            it.localPlanId = localPlanId
            it.updateTime = updateTime
        }
//        Log.v("shero", "(${planId}) ${plan.param?.toLog()}")
        return plan
    }
}

fun Plan.toPlanInfo(): PlanInfo {
    return PlanInfo(
        routeMode,
        routePointToListString(track).toTypedArray(),
        width,
        height,
        speed,
        drugQuantity,
        drugFix,
        blockId,
        param?.toString()
    ).also {
        it.planId = planId
        it.localPlanId = localPlanId
        it.localBlockId = localBlockId
        it.graphId = graphId
        it.naviArea = naviArea
        it.updateTime = updateTime
    }
}

@Keep
class SortieInfo(
    val id: String,  //飞机ID
    val sortie: Int, //架次ID
    val sprayWidth: Float,
    val cropType: Int,
    val drug: Float,  //药量
    val area: Float, //亩数
    val track: List<String>,

    var blockId: Long?,//线上blockId
    val groupId: Long?,
    val planPercent: Int,
    val flightime: Int,
    val totalArea: Double, //米
    var componentVersion: String
) {
    var planId: Long = 0//线上planId
    var additional: String = ""

    var localBlockId: Long = 0//本地blockId
    var localPlanId: Long = 0//本地planId

    var userId: Long = 0
    var startTime: Long = 0
    var endTime: Long = 0

    var posData: List<String>? = null
    var route: List<String> = listOf()
    var lat0: Double = 0.0
    var lng0: Double = 0.0
    var supplement: String = ""
    var workType: Int = DroneModel.TYPE_SPRAY
    var workMode: Int = DroneModel.TYPE_MODE_MA
    var battery: List<String> = listOf()

    override fun toString(): String {
        return String.format(
            Locale.US,
            "S:%s,%d,%.2f,%d,%.2f,%.2f,%d," +
                    "remoteBlockId[%d],%d,%d,%d,%.2f,%s," +
                    "remotePlanId[%d],[%s],blockId[%d],planId[%d],%d,%d,%d,%d,%d," +
                    "%s,%d,%d,%d",
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
            planId ?: 0,
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
            battery.size
        )
    }
}

//我的-我的设备-架次信息
@Keep
class DeviceSortieInfo(
    val address: String,
    val yyAccountName: String?,
    val droneId: String,
    val userName: String, val userPhone: String,
    val startTime: Long, val endTime: Long,
    val flightTime: Int,
    val sprayWidth: Float,
    val sprayArea: Float, val sprayCapacity: Float,
    val jobType: Int, val jobMode: Int
)

fun Sortie.toSortieInfo(): SortieInfo {
    this.let {
        val sortie = SortieInfo(
            id, sortie, sprayWidth, cropType, drug, area, trackNodeToList(track),
            blockId, groupId, planPercent, flightime, totalArea, componentVersion
        )
        sortie.planId = planId
        sortie.additional = sortieAdditionalToString(additional) ?: ""
        sortie.localBlockId = localBlockId
        sortie.localPlanId = localPlanId
        sortie.userId = userId
        sortie.posData = if (posData == null) null else posToListString(posData!!)
//        logToFile("sortie route:${route} sortie:$sortie")
        sortie.route = routePointToListString(route)
        return sortie
    }
}

@Keep
class DroneParam(val type: Int, val paramName: String, var param: String?) {
    var paramId: Long = 0
    var isLocal: Boolean = false
    var localId: Long = 0

    constructor(
        type: Int,
        paramName: String,
        param: String?,
        isLocal: Boolean,
        paramId: Long,
        localId: Long
    ) : this(type, paramName, param) {
        this.isLocal = isLocal
        this.paramId = paramId
        this.localId = localId
    }
}

@Keep
class ChartData(
    val id: Long,
    var name: String,
    var param: String?,
    val type: Int?,
    val data: IProtocol.UserData?
)

@Keep
class AppLog(
    @LOG_TYPE val type: Int,
    val logTime: Long, val note: String,
    val fileName: String,
    val flavor: String, val abi: String, val versionRelease: String, val version: String,
    val model: String, val product: String, val manufacturer: String,
    val appCode: String, val appVersion: String, val userId: Long
) {
    var fileUrl: String = ""
}

const val LOG_TYPE_APP = 1//app日志
const val LOG_TYPE_CRASH = 2//崩溃日志

@Retention(AnnotationRetention.SOURCE)
@IntDef(LOG_TYPE_APP, LOG_TYPE_CRASH)
annotation class LOG_TYPE

@Keep
class DroneLog(val createTime: Long, val url: String, val droneId: String)

@Keep
class AppUIConfig(
    val accountId: Long,
    val launchPageUrl: String?,
    val homePageUrl: String?,
    val theme: String?,
    val servAddr: String?
) {
    override fun toString(): String {
        return "accountId:$accountId,launchPageUrl:$launchPageUrl,homePageUrl:$homePageUrl,theme:$theme,appAddr:$servAddr"
    }
}

@Keep
data class DroneDevice(
    val droneId: String,
    val droneName: String?,   // 名称
    val zzDroneNum: String?,   // 名称
    val modelName: String,   // 机型
    val oprName: String?,   // 飞手（列表里的）
    val oprUserName: String?,   // 飞手（设备详情里的）
    val status: Int,   // 状态
    val pictureUrl: String,   // 设备照片
    val auth: Int,           //是否有分配？修改？锁定的权限  1 有  0 无
    val activeTime: String,
    val zzAccountName: String,
    val droneIsLock: Int,  // 0 未锁定，1 锁定，2 APP锁
    val zzIsLock: Int,  // 制造商锁：1 未锁，2 锁定，3 APP锁
    val yyIsLock: Int, // 当前账户锁：1 未锁，2 锁定，3 APP锁
    val rtkStartTime: Long,
    val rtkEndTime: Long?,
    val rtkPrice: Float?,
    val rackNo: String?,
    var activeStatus: Int?,  // 飞机激活状态0=调试  1=待激活  2=激活
) {
    val userId: Long? = null
}

@Keep
data class DroneList(val stat: DroneStat) : Page<DroneDevice>(0) {
    @Keep
    class DroneStat(val workArea: Float, val sortieCount: Int, val flightTime: Long)
}

@Keep
data class DroneDetail(val staticInfo: DroneDevice, val dynamicInfo: UserStatistic)

@Keep
class FlyHistoryStatic(
    val sortieCount: Int,
    val flightTime: Float,
    val sprayRange: Float,
    val sprayCapacity: Float,
    val mileage: Float
)

@Keep
class TeamWorkReport(
    droneId: String,   // 飞机ID
    sortieId: Long,   // 架次ID
    createTime: Long,   // 飞行开始时间
    operName: String,   // 飞手姓名
    isAuto: Boolean,   //  是否为自动
    regionName: String,   // 地区名称
    sprayRange: Float,   // 喷洒面积
    val userId: Long
) : SortieItem(droneId, sortieId, createTime, operName, isAuto, regionName, sprayRange)

@Keep
data class Region(
    val regionName: String,
    val cityName: String,
    val provinceName: String,
    val detailName: String
)

@Keep
data class SelectTeam(
    val groupId: Long,
    val groupName: String
)

@Keep
data class SelectOper(
    val userId: Long,
    val username: String
)

@Keep
class UpdateDroneNameInfo(val droneId: String, val droneName: String)

@Keep
class UpdateZzDroneNameInfo(val zzDroneNum: String)

@Keep
class DroneLockInfo(val droneId: String, val opr: Int) {
    companion object {
        const val UNLOCK = 1
        const val LOCK = 2
        const val APP_LOCK = 3
    }
}

@Keep
class CreateGroupInfo(val groupName: String)

//团队
@Keep
data class Team(
    val groupId: Long,   // groupId
    val groupName: String,   // 名称
    val accountId: Long,
    val leaderUserId: Long,
    val createTime: Long,
    val current: Boolean,
    val inGroup: Boolean,
    val hasAuth: Boolean
) {
    val identity: Int
        get() =
            if (leaderUserId == AgsUser.userInfo?.userId) {
                LEADER
            } else {
                MEMBER
            }

    companion object {
        const val LEADER = 0   // 队长
        const val MEMBER = 1   // 队员
    }
}


/**
 * 团队详情
 */
@Keep
data class GroupDetail(
    val groupId: Long,
    var groupName: String,
    val leaderUserId: Long,
    val taskCount: Int,
    val sprayArea: Float,
    val userCount: Int,
    val hasAuth: Boolean,
    val inGroup: Boolean
)

@Keep
data class TeamEmployee(
    val userId: String,   // 用户Id
    val username: String,   // 员工名
    val userHeadUrl: String,   //员工头像
    var isSelected: Boolean   // 是否选中
)

/**
 * 成员
 */
@Keep
data class Member(
    val userId: Long,
    val userHeadUrl: String?,
    val username: String,
    val identify: Int,
    val userphone: String,
    val addToGroupTime: Long
) : Serializable

@Keep
data class UserWorkStatic(
    val sortieCount: Int,
    val flightTime: Float,
    val sprayRange: Float,
    val sprayCapacity: Float,
    val mileage: Float
)

@Keep
data class MemberReportDetail(
    val droneId: String,
    val sortieId: Long,
    val createTime: Long,
    val operUserName: String,
    val isAuto: Boolean,
    val regionName: String,
    val sprayRange: Float
)

@Keep
class UpdateGroupNameInfo(val groupName: String, val groupId: Long)

@Keep
class AddGroupMemberInfo(val groupId: Long, val userIds: String?, val phones: String?)

@Keep
class TransferLeaderInfo(val groupId: Long, val leaderUserId: Long)

@Keep
class DeviceUseRecord(val deviceId: String, val userId: Long?, val appVersion: String)

@Keep
class NoFlyZoneInfo(
    val effectStartTime: Long,
    val orbitStr: String,
    val detailAddress: String,
    val noflyId: Int,
    val effectEndTime: Long,
    val orbit: String,
    val noflyType: Int,
    val isEnable: Int,
    val effectStatus: Int,
) {

    companion object {
        //将str转换成边界点
        fun convertBoundary(orbit: String): List<GeoHelper.LatLng> {
            val type = TypeToken.getParameterized(List::class.java, LngLat::class.java).type
            val jsonData = Gson().fromJson<List<LngLat>>(orbit, type)
            return jsonData.map {
                GeoHelper.LatLng(it.lat, it.lng)
            }
        }
    }

    fun toLocalData(): LocalNoFlyZone {
        val json = Gson().toJson(this)
        val center = centerOfMapRing(convertBoundary(this.orbit))
        val localData = Gson().fromJson(
            json,
            LocalNoFlyZone::class.java
        )
        localData.lat = center.latitude
        localData.lng = center.longitude
        return localData
    }
}

@Keep
class LngLat(val lng: Double, val lat: Double)

@Keep
class IdentityInfo(val idcardNum: String, val contactName: String)

@Keep
class ServiceResult<T>(val code: Int, val msg: String, data: T)

@Keep
class IdentityVerify(val userId: String, val verifyState: Boolean, val verifyInfo: VerifyInfo?) {
    @Keep
    class VerifyInfo(val name: String, val identityNumber: String)
}

//团队
class Group(var groupId: Long, var groupName: String)