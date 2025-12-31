package com.jiagu.ags4.vm

import androidx.annotation.Keep

@Keep
data class Region(
    val regionName: String,
    val cityName: String,
    val provinceName: String,
    val detailName: String
)

//作业类型
@Keep
data class WorkType(
    val workTypeId: Int,//作业类型ID
    val workTypeName: String//作业类型名称
)

//任务详情(/tasks/{taskId})
@Keep
data class TaskDetail(
    val estimateArea: Float,//预估面积
    val sprayArea: Float,//已作业亩数
    var taskPercent: Int,//已作业进度
    var blockCount: Int,//总地块数
    var workBlockCount: Int,//已工作地块数
    var drugQuantity: Float,//用药量
    var groupCount: Int,//总团队数
    var monitorUserCount: Int,//已添加团队数量
    var taskName: String,//任务名称
    var taskNum: String,//任务编号
    var startTime: Long,//任务开始时间
    var region: Int,//区域编码
    var regionName: String,//区域名称
    var address: String,//详细信息
    var cropIds: String,//作物名Id
    var cropNames: List<String>,//作物名称
    var workTypeIds: String,//作业类型Ids
    var workTypeNames: List<String>,//作业类型名称
    var userName: String,//任务发起人名
    var workIds: String,
    var isComplete: Boolean,
    var sortieCount: Long,
    var workTypes: List<WorkType>
)

@Keep
class UserSortieQueryParams(val userIds: List<String>, val startTime: String, val endTime: String, val pageIndex: Int, val size: Int,val jobType:String)

@Keep
class UserSortieCount(val flightTime : Long, val sprayArea : Float, val sprayQuantity : Float, val seedingQuantity : Float, val flightCount : Int, val days: Int, val startDate: Long)

@Keep
class UserSortieCountDetail(val id:Long, val sprayArea : Float, val sprayQuantity : Float, val seedingQuantity : Float, val userName : String, val startTime: Long, val endTime: Long, val regionName: String?, val sortieId: Long, val droneId: String)

@Keep
class FlyHistoryDetail(
    val startTime: Long,
    val endTime: Long,
    val isAuto: Boolean,
    val region: Region,
    val blockNum: String,
    val sprayRange: Float,
    val sprayCapacity: Float,
    val sprayWidth: Float,
    val groupName: String,
    val operUserName: String,
    var droneName: String,
    val accountName: String,
    val workId: Long,
    val concatPhone: String?,
    val blockId: Long,
    val task: TaskDetail
)