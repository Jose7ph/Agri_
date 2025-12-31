package com.jiagu.ags4.vm.work

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.WorkUtils
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.NaviTask
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.math.Point2D
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.tools.v9sdk.CleanUtils
import kotlinx.coroutines.flow.MutableStateFlow


interface IWorkClean {
    var width: Float
    var repeatCount: Int
    var speed: Float
    val repeatRoutes: MutableList<RoutePoint>
    val blockPoints2D: MutableStateFlow<List<Point2D>>
    val workPoints2D: MutableStateFlow<List<Point2D>>
    var dronePoint2D: Point2D?
    var breakPoint2D: Point2D?
    var routeYaw: Float?

    fun clearWorkPoints()

    fun uploadNavi(isABMode: Boolean, breakPoint: VKAg.BreakPoint?): NaviTask?

    fun checkCleanModel(mode: Int, localBlockId: Long? = null)
    fun initClean()

    suspend fun calcRoutePoint(
        boundary: List<Point2D>,
        pts: List<Point2D>,
        routes: List<RoutePoint>?,
    )
}

class WorkCleanImpl : IWorkClean {
    override var width by mutableFloatStateOf(1f)
    override var repeatCount by mutableIntStateOf(0)
    override var speed by mutableFloatStateOf(1f)
    override val repeatRoutes = mutableListOf<RoutePoint>()
    override val blockPoints2D = MutableStateFlow<List<Point2D>>(emptyList())
    override val workPoints2D = MutableStateFlow<List<Point2D>>(emptyList())
    override var dronePoint2D by mutableStateOf<Point2D?>(null)
    override var breakPoint2D by mutableStateOf<Point2D?>(null)
    override var routeYaw: Float? = null
    override fun initClean() {
        clearWorkPoints()
        width = 1f
        speed = 1f
        repeatCount = 0
    }

    override suspend fun calcRoutePoint(
        boundary: List<Point2D>,
        pts: List<Point2D>,
        routes: List<RoutePoint>?,
    ) {
        blockPoints2D.emit(boundary)
        if (!routes.isNullOrEmpty()) {
            //2D航线 上传到飞控的航线
            val point2Ds = CleanUtils.repeatPoint2Ds(pts, repeatCount)
            val routePoints = CleanUtils.repeatRoutePoints(routes, repeatCount)
            setWorkPoint(point2Ds, routePoints)
        }
    }

    override fun clearWorkPoints() {
        repeatRoutes.clear()
        routeYaw = null
        workPoints2D.value = listOf()
        dronePoint2D = null
        breakPoint2D = null
        blockPoints2D.value = listOf()
    }

    override fun uploadNavi(isABMode: Boolean, breakPoint: VKAg.BreakPoint?): NaviTask? {
        if (repeatRoutes.isEmpty()) {
            return null
        }
        DroneModel.activeDrone?.let {
            it.clearPosData()
            DroneModel.breakPoint.value = null
            val task = NaviTask(it)
            var home: GeoHelper.LatLngAlt? = null
            it.homeData.value?.apply {
                home = GeoHelper.LatLngAlt(lat, lng, alt)
            }
            val d = UploadNaviData(
                0f,
                speed,
                width,
                0f,
                AptypeUtil.getPumpAndValve(),
                0,
                2
            )
            val r = mutableListOf<RoutePoint>()
            //固定下降的点 默认第二个点下降 如果有重复就加上重复次数
            val downPointStep = 2 + repeatCount
            for ((i, p) in repeatRoutes.withIndex()) {
                val wpParam = if ((i + 1) % downPointStep == 0) 3 else 1
                val rp = RoutePoint(p.latitude, p.longitude, true, i + 1)
                rp.height = p.height
                rp.heightType = 0
                rp.routeType = VKAg.MISSION_UTYPE_AB
                rp.wpParam = wpParam
                rp.wlMission = 4
                routeYaw?.let { yaw ->
                    rp.wlParam = yaw * 10
                }
                r.add(rp)
            }
            d.route = r
            //设置断点
            breakPoint?.let { bk ->
                DroneModel.breakPoint.value = bk
            }
            LogFileHelper.log("${if (isABMode) "AB" else "区域"}清洗模式航点:${r}")
            val cmds = mutableListOf<() -> Unit>()
            cmds.add { DroneModel.activeDrone?.setNaviProperties(d) }
            cmds.add { AptypeUtil.setPumpMode(VKAg.BUMP_MODE_FIXED) }
            d.commands = cmds
            task.setParam(home, d)
            return task
        }
        return null
    }

    private var preCleanMode = 0
    override fun checkCleanModel(mode: Int, localBlockId: Long?) {
        if (VKAgTool.isCleanMode(mode)) preCleanMode = mode
        if (VKAgTool.isCleanMode(preCleanMode) && mode != preCleanMode) {
            if (localBlockId != null) {
                DroneModel.getBreakPoint(localBlockId)
            } else {
                DroneModel.activeDrone?.getBreakPoint()
            }
            preCleanMode = mode
        }
    }

    private suspend fun setWorkPoint(pts: List<Point2D>, data: List<RoutePoint>) {
        repeatRoutes.clear()
        repeatRoutes.addAll(data)
        workPoints2D.emit(pts)
    }
}