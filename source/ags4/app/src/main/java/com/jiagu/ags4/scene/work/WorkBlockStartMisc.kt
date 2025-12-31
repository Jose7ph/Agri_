package com.jiagu.ags4.scene.work

import com.jiagu.ags4.R
import com.jiagu.ags4.vm.BlockParamModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.model.UploadNaviData
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.tools.v9sdk.OutPathRouteModel
import com.jiagu.tools.v9sdk.RouteModel


fun MapVideoActivity.collectBlockStart(
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    taskModel: TaskModel,
    outPathRouteModel: OutPathRouteModel,
) {
    auxReq.value = 0
    showMarker = false
    startDataListener()
    collectFlow(routeModel.route) { track ->
        when {
            track.isNullOrEmpty() -> clearTrack()
            track.size > 800 -> toast(R.string.plan_toomany_wp)
            else -> {
                showTrack(track, blockParamModel.planType) { allMarkerMap ->
                    blockParamModel.curMarkerCount =
                        allMarkerMap.filter { it.key.startsWith("route_") }.size
                    blockParamModel.curAllMarkers = allMarkerMap
                }
            }
        }
    }
    //画地块和障碍物
    collectFlow(blockParamModel.selectedBPFlow) {
        if (it.isNotEmpty()) {
            val block = it[0]
            canvas.drawBlock(
                "block", block.boundary[0], false
            )
            drawBarriers(canvas, block.barriers)
        }
    }
    //中转点
    collectFlow(blockParamModel.auxPointsFlow) {
        findStartAuxPath(routeModel, blockParamModel, taskModel, outPathRouteModel) {}
    }
    collectFlow(auxReq) {
        it.let {
            when (it) {
                2 -> {
                    checkEndAUX2(blockParamModel, routeModel, outPathRouteModel) {
                        hideDialog()
                    }
                }
            }
        }
    }

    collectFlow(taskModel.breakWpFlow) {
        if (it == null) {
            DroneModel.bk = null
            removeBreakPoint()
        } else {
            DroneModel.bk = it//架次中的断点需要的
            drawBreakpoint(it.lat, it.lng)

            //航线重排需要的，要把断点之前的点都去掉
            routeModel.bk = GeoHelper.LatLngAlt(it.lat, it.lng, it.alt.toDouble())
            routeModel.target = it.index.toInt() + 1
        }
    }

    drawWorkLine()
    drawCalcBreaks(taskModel)
    clearBreaksByIMU(taskModel)
}

fun MapVideoActivity.checkEndAUX2(
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    outPathRouteModel: OutPathRouteModel,
    complete: (Boolean) -> Unit,
) {
    findEndAuxPath(blockParamModel, routeModel, outPathRouteModel) {
        if (blockParamModel.calcAuxPoints.isNotEmpty()) {
            DroneModel.activeDrone?.responseToReturnNavi(false)
            val path = mutableListOf<RoutePoint>()
            for (a in blockParamModel.calcAuxPoints) {
                path.add(RoutePoint(a.latitude, a.longitude, false, 0))
            }
            uploadAux(this, UploadNaviData.TYPE_AUX_E, path, blockParamModel) { success, _ ->
                complete(success)
                false
            }
        } else {
            if (blockParamModel.auxPoints.isEmpty()) DroneModel.activeDrone?.responseToStartNavi(
                true
            )
            else {
                val path = mutableListOf<RoutePoint>()
                for (s in blockParamModel.auxPoints.size - 1 downTo 0) {
                    path.add(
                        RoutePoint(
                            blockParamModel.auxPoints[s].latitude,
                            blockParamModel.auxPoints[s].longitude,
                            false,
                            0
                        )
                    )
                }
                uploadAux(this, UploadNaviData.TYPE_AUX_E, path, blockParamModel) { success, _ ->
                    complete(success)
                    false
                }
            }
        }
    }
}

fun MapVideoActivity.findEndAuxPath(
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    outPathRouteModel: OutPathRouteModel,
    complete: () -> Unit,
) {
    blockParamModel.calcAuxPoints.clear()
    outPathRouteModel.findEndPathByBarrier(
        if (DroneModel.imuData.value == null) null else GeoHelper.LatLng(
            DroneModel.imuData.value!!.lat, DroneModel.imuData.value!!.lng
        ),
        blockParamModel.auxPoints,
        blockParamModel.obstacles,
        blockParamModel.block,
        if (DroneModel.homeData.value == null) null else GeoHelper.LatLng(
            DroneModel.homeData.value!!.latitude, DroneModel.homeData.value!!.longitude
        ), blockParamModel.barrierSafeDist
    ) {
        if (it == null) {
            blockParamModel.calcAuxPoints.addAll(listOf())
            blockParamModel.addAuxWarn()
        } else if (it.isEmpty()) {
            blockParamModel.calcAuxPoints.addAll(listOf())
        } else blockParamModel.calcAuxPoints.addAll(it)
        drawEndCalcAuxPoints(blockParamModel)
        complete()
    }
}

fun MapVideoActivity.drawEndCalcAuxPoints(blockParamModel: BlockParamModel) {
    drawAuxPoint(blockParamModel.auxPoints)
    if (blockParamModel.calcAuxPoints.isEmpty()) {
        if (blockParamModel.auxPoints.isNotEmpty()) {
            drawAuxLine(blockParamModel.auxPoints)
        } else {
            removeAuxLine()
        }
        return
    }
    val mutableList = blockParamModel.calcAuxPoints.toMutableList()
    DroneModel.imuData.value?.let {
        mutableList.add(
            0, GeoHelper.LatLng(DroneModel.imuData.value!!.lat, DroneModel.imuData.value!!.lng)
        )
    }
    DroneModel.homeData.value?.let { mutableList.add(it) }
    drawAuxLine(mutableList)
}