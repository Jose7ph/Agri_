package com.jiagu.ags4.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiagu.ags4.bean.SortieRoute
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.utils.logToFile
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.tools.v9sdk.TaskModelUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TaskModel : ViewModel() {

    private var ptNavi: List<RoutePoint>? = null
    private val ptTrack = mutableListOf<Point2D>()

    private lateinit var converter: GeoHelper.GeoCoordConverter

    private var done = false

    var breakWp: VKAg.BreakPoint? = null

    //画起始航线用，在地图上画断点用，航线重排使用断点信息时用（根据飞控里的断点，在航线上优化过的点）
    val breakWpFlow = MutableStateFlow<VKAg.BreakPoint?>(null)

    private var planType = RouteModel.PLAN_BLOCK
    fun setupNavi(track: List<RoutePoint>, type: Int) {
        planType = type
        converter = GeoHelper.GeoCoordConverter()
        ptNavi = track
        converter.convertLatLng(track, ptTrack)
        var path = 0.0
        for (i in 0 until track.size - 1) {
            val d = ptTrack[i].distance(ptTrack[i + 1])
            if (track[i].pump) path += d
        }
    }

    fun setBreakPoint(bp: VKAg.BreakPoint?) {//找断点在航线上的垂线，找到后修改掉断点的经纬度  目标点序号-1
        breakWp = bp
        if (breakWp == null || ptNavi == null || ptNavi?.isEmpty() == true) {
            emitBreakPoint()
            return
        }
        breakWp?.let {
            val bk = TaskModelUtils.getFormatBreakPoint(it.index.toInt(), it.lat, it.lng, ptNavi!!, converter)
            bk?.let {
                breakWp?.lat = bk.latitude
                breakWp?.lng = bk.longitude
                emitBreakPoint()
            }
        }
    }

    private fun emitBreakPoint() {
        exeTask {
            DroneModel.breakPoint.value = breakWp
            breakWpFlow.emit(breakWp)
        }
    }

    private fun naviComplete() {
        breakWp = null
        done = true
        emitBreakPoint()
    }

    fun clearBK() {
        breakWp = null
        emitBreakPoint()
    }

    fun clearDone() {
        done = false
    }

    fun clearCalcBK(){
        selectBreakIndex = -1
        curCalcBK = null
    }

    val calcBreaks = MutableStateFlow<List<VKAg.BreakPoint>>(listOf())
    var curCalcBK: VKAg.BreakPoint? = null
    private var droneLocation: GeoHelper.LatLng? = null
    private var imuTime = 0L
    private var calc = false
    private var preFlyRouteMode = -1
    private var preFlyStartMode = -1
    var selectBreakIndex = -1
    var showBreak by mutableStateOf(false)
    private var airFlag = VKAg.AIR_FLAG_ON_GROUND

    private var current = System.currentTimeMillis()
    fun setImuData(imu: VKAg.IMUData) {
        droneLocation = GeoHelper.LatLng(imu.lat, imu.lng)
        checkFlyMode(imu)
        current = System.currentTimeMillis()
        if (calc && imu.airFlag == VKAg.AIR_FLAG_ON_AIR) {
            if (current - imuTime > 1000 && breakWp != null && ptNavi != null && ptNavi!!.isNotEmpty()) {
                imuTime = current
                if (planType == VKAg.MISSION_FREE) emitBreakPoints(TaskModelUtils.generateBreakPointFree(droneLocation!!, breakWp!!, ptNavi!!, planType))
                else emitBreakPoints(TaskModelUtils.generateBreakPointBlock(droneLocation!!, breakWp!!, ptNavi!!, planType))
            }
        } else {
            if (!calc) return
            endCalcBreak()
            clearBreaks()
        }
    }

    private fun checkFlyMode(imuData: VKAg.IMUData) {
        droneLocation = GeoHelper.LatLng(imuData.lat, imuData.lng)
        if (imuData.airFlag == VKAg.AIR_FLAG_ON_GROUND) return
        if (VKAgTool.isBack(imuData.flyMode.toInt())) { //返航/降落..模式停止计算断点12 并且清除断点12
            endCalcBreak()
            clearBreaks()
        }
        if (VKAgTool.isNavigation(imuData.flyMode.toInt())) {
            preFlyRouteMode = imuData.flyMode.toInt()
        }
        //自由航线左右拨杆时，不会变成GPS模式，用户需要自己切模式才开始计算断点12
        if ((VKAgTool.isNavigation(preFlyRouteMode)) && VKAgTool.isGpsMode(imuData.flyMode.toInt()) && !done) {//之前是航线模式/AB模式 现在是GPS模式 开始计算断点
            calc = true
            checkShowBreak(calc)
        }
    }

    fun checkStartEndMode(imuData: VKAg.IMUData): Boolean {
        if (airFlag == VKAg.AIR_FLAG_ON_AIR && imuData.airFlag == VKAg.AIR_FLAG_ON_GROUND) {
            preFlyStartMode = -1
            airFlag = imuData.airFlag
            return true
        }
        airFlag = imuData.airFlag
        if (VKAgTool.isStartEndMode(imuData.flyMode.toInt())) preFlyStartMode =
            imuData.flyMode.toInt()
        if (VKAgTool.isStartEndMode(preFlyStartMode) && imuData.flyMode == VKAgCmd.FLYSTATUS_ZIDONGHANGXIAN) {
            preFlyStartMode = -1
            return true
        }
        return false
    }

    private fun emitBreakPoints(data: List<VKAg.BreakPoint>) {
        exeTask { calcBreaks.emit(data) }
    }

    fun clearBreaks() {
        calcBreaks.value = listOf()
    }

    fun endCalcBreak() {
        calc = false
        imuTime = 0
        preFlyRouteMode = -1
        checkShowBreak(false)
    }

    private fun checkShowBreak(show: Boolean) {
        showBreak = show
    }

    fun checkNaviDone(data: VKAg.IMUData, localBlockId: Long, complete: () -> Unit) {
        if (ptNavi == null || ptNavi!!.isEmpty() || done) return
        viewModelScope.launch {
            if (data.airFlag == VKAg.AIR_FLAG_ON_AIR && ptNavi != null && ptNavi!!.isNotEmpty() && !VKAgTool.isNavigation(data.flyMode.toInt())) {
                val wt = ptNavi
                val targetDist = isComeLastPoint(ptNavi, GeoHelper.LatLng(data.lat, data.lng))
                if ((data.flyMode == VKAgCmd.FLYSTATUS_GCSFANHANG && data.returnReason == VKAgCmd.GOHOME_REASON_HANGXIANWANCHENG && wt != null && data.target >= wt.size) ||//19 & 17
                    (data.flyMode == VKAgCmd.FLYSTATUS_GCSXUANTING && data.hoverReason == VKAgCmd.HOVER_REASON_HANGXIANWANCHENG && wt != null && data.target >= wt.size) ||//18 & 3
                    (wt != null && data.target > wt.size) ||
                    (wt != null && data.target.toInt() == wt.size && targetDist)
                ) {
                    logToFile("task model done flyMode:${data.flyMode} returnReason:${data.returnReason} hoverReason:${data.hoverReason} target:${data.target} wt.size:${wt?.size} targetDist:${targetDist}")
                    naviComplete()
                    val list = mutableListOf<GeoHelper.LatLngAlt>()
                    for (i in 0 until wt.size) {
                        list.add(GeoHelper.LatLngAlt(wt[i].latitude, wt[i].longitude, wt[i].height.toDouble()))
                    }
                    val tmpArea = data.ZuoYeMuShu - DroneModel.sortieArea0
                    var tmpDrug = data.YiYongYaoLiang
                    DroneModel.sortieDrug = tmpDrug
                    if (tmpDrug < 0) tmpDrug = 0f
                    if (tmpArea > DroneModel.sortieArea && tmpArea < 1000) {
                        DroneModel.sortieArea = tmpArea.toFloat()
                        DroneModel.sortieDrug = tmpDrug
                        DroneModel.sortieAreaData.postValue(tmpArea.toFloat())
                    }
                    Repo.addWorkArea(
                        localBlockId,
                        DroneModel.sortieArea.toDouble(),
                        DroneModel.sortieDrug.toDouble(),
                        SortieRoute(ptNavi),
                        100,
                        null,
                        list
                    ) {
                        Repo.workFinish(localBlockId)
                        complete()
                    }
                }
            }
        }
    }

    private fun isComeLastPoint(data: List<RoutePoint>?, dronePosition: GeoHelper.LatLng): Boolean {
        val wts = data
        if (wts.isNullOrEmpty()) return false
        val converter = GeoHelper.GeoCoordConverter()
        val p1 = converter.convertLatLng(dronePosition)
        val p2 = converter.convertLatLng(wts.last())
        val dist = p1.distance(p2)
        return dist < 5.00
    }
}