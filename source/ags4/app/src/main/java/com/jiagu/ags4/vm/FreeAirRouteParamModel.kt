package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.IWorkMapMarker
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.WorkMapMarkerImpl
import com.jiagu.ags4.vm.work.IWorkEditCanvas
import com.jiagu.ags4.vm.work.IWorkParameter
import com.jiagu.ags4.vm.work.IWorkPlan
import com.jiagu.ags4.vm.work.WorkEditCanvasImpl
import com.jiagu.ags4.vm.work.WorkParameterImpl
import com.jiagu.ags4.vm.work.WorkPlanImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.tools.v9sdk.RouteModel

class FreeAirRouteParamModel(app: Application) : AndroidViewModel(app),
    IWorkEditCanvas by WorkEditCanvasImpl(),
    IWorkParameter by WorkParameterImpl(),
    IWorkPlan by WorkPlanImpl(),
    IWorkMapMarker by WorkMapMarkerImpl() {
    val context = getApplication<Application>()
    private val config = Config(context)
    var isInit = false

    //当前plan类型 0-新规划 1-开始作业
    var curPlanType = RouteModel.PLAN_NEW

    var wayLineType by mutableIntStateOf(0)  // 0-果树 1-自由航点

    var pointPump by mutableIntStateOf(1)//航点喷洒默认开
    var naviPump by mutableIntStateOf(0)//航线喷洒全部：默认关
    var pointPumpTime by mutableIntStateOf(5)//航点喷洒时间默认5秒
    var pointHeightType by mutableIntStateOf(1)//0航点高度/1海拔高度

    var workTrack = mutableListOf<RoutePoint>()
    var heightDif by mutableFloatStateOf(0f) // 高度差

    fun initBlockPlanAndParam(blockPlan: BlockPlan, isNewPlan: Boolean) {
        exeTask {
            setBlockPlan(blockPlan)
            val boundary = blockPlan.boundary
            if (boundary.isNotEmpty()) {
                addPoints(boundary[0])
            }
            initWorkParam()
            workTrack.clear()
            workTrack.addAll(initAirRouteTrack(blockPlan))
            initPlanParam(blockPlan)
            curPlanType = if (isNewPlan) RouteModel.PLAN_NEW else RouteModel.PLAN_LAST_WORK
            if (curPlanType == RouteModel.PLAN_LAST_WORK) {
                DroneModel.blockPlan.emit(blockPlan)
            } else {
                DroneModel.blockPlan.emit(null)
            }
            DroneModel.blockId = blockPlan.blockId
            DroneModel.localBlockId = blockPlan.localBlockId
            DroneModel.workRoutePoint.clear()
        }
    }

    private fun initWorkParam() {
        speed = AptypeUtil.getABSpeed()
        mode = AptypeUtil.getPumpMode()
        pumpOrValveSize = AptypeUtil.getPumpAndValve().toInt()
        sprayOrSeedMu = AptypeUtil.getSprayMu()
    }

    private fun initPlanParam(blockPlan: BlockPlan) {
        blockPlan.plan?.let {
            workPlan = it
            if(it.track.isNotEmpty()){
                pointPump = if (it.track.any {r -> r.pump }) { 1 } else { 0 }
                pointHeightType =it.track[0].heightType
                naviPump = if (it.track.any {r -> r.wlMission == VKAgCmd.WL_MISSION_PUMP.toInt() }) { 1 } else { 0 }
            }
            workTrack.clear()
            workTrack.addAll(it.track)
            it.param?.let { param ->
                updatePlanParamByCurrentParam(param)
            }
        }
    }

    fun updatePlanParamByCurrentParam(param: PlanParamInfo) {
        speed = param.speed
        mode = when {
            param.pumpMode != 0 -> param.pumpMode
            param.seedMode != 0 -> param.seedMode
            else -> {
                AptypeUtil.getPumpMode()
            }
        }
        pumpOrValveSize = when {
            param.valveSize != 0 -> param.valveSize
            param.pumpSize != 0f -> param.pumpSize.toInt()
            else -> AptypeUtil.getPumpAndValve().toInt()
        }
        sprayOrSeedMu = when {
            param.sprayMu != 0f -> param.sprayMu
            param.seedMu != 0f -> param.seedMu
            else -> AptypeUtil.getSprayMu()
        }

    }

    private fun initAirRouteTrack(bp: BlockPlan): List<RoutePoint> {
        val navi = mutableListOf<RoutePoint>()
        val route = bp.boundary[0]
        for ((i, pt) in route.withIndex()) {
            val rp = RoutePoint(pt.latitude, pt.longitude, pointPump == 1, i)
            rp.routeType = VKAg.MISSION_FREE
            val alts = pt.altitude.toFloat()
            if (alts != 0f) {
                rp.height = alts
                rp.elevation = alts
            }
            rp.index = i
            rp.wlMission = naviPump //航线喷洒 0关1开
            rp.heightType = pointHeightType //航点高度类型 0 相对 1海拔
            rp.wpParam = pointPumpTime//航点喷洒时间 默认5秒
            navi.add(rp)
        }
        return navi
    }

    fun changeMarkerPoint(isNext: Boolean = false) {
        var markerPosition: GeoHelper.LatLngAlt? = null
        var curIdx = selectedMarkerIndex
        if (points.size > 1) {
            curIdx = when (selectedMarkerIndex) {
                points.lastIndex -> { //最后一个索引
                    if (isNext) 0 else curIdx - 1
                }

                0 -> {//第一个索引
                    if (isNext) curIdx + 1 else points.lastIndex
                }

                else -> { //中间
                    if (isNext) curIdx + 1 else curIdx - 1
                }
            }
            markerPosition = points[curIdx]
        }
        markerPosition?.let {
            changeSelectedMarkerIndex(curIdx)
            initMarker(it)
        }
    }

    fun saveOrUpdatePlan(isUpdate: Boolean, complete: () -> Unit) {
        exeTask {
            workPlan?.let {
                if (!isUpdate) {
                    savePlan(it, complete)
                } else {
                    updatePlan2(it, complete)
                }
            }
        }
    }

    fun getBlockPlan(complete: (BlockPlan?) -> Unit) {
        exeTask {
            getLocalBlockPlan {
                complete(it)
            }
        }
    }

    fun blockWorking(complete: () -> Unit = {}) {
        exeTask {
            selectedLocalBlockId?.let {
                blockWorking(it, complete = complete)
            }
        }
    }
}