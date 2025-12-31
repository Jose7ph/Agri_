package com.jiagu.ags4.scene.work

import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.FreeAirRouteParamModel
import com.jiagu.ags4.vm.TaskModel

fun MapVideoActivity.collectFreeAirRouteStart(
    freeAirRouteParamModel: FreeAirRouteParamModel,
    taskModel: TaskModel,
) {
    //地图点击
    addMapClickListener(FreeAirRouteParamMapClickListener(canvas, freeAirRouteParamModel))
    //航点点击
    addMarkClickListener(FreeAirRouteParamMarkerClickListener(canvas, freeAirRouteParamModel))

    //选择的marker点
    collectFlow(freeAirRouteParamModel.selectedMarkerIndexFlow) {
        unhighlightMarker(canvas)
        if (it >= 0) {
            highlightMarker(canvas, it)
        }
    }

    //点
    collectFlow(freeAirRouteParamModel.pointsFlow) {
        if (it.isEmpty()) {
            canvas.remove("edge")
            clearNumbers()
            droneCanvas.clearText()
        } else {
            if (it.size == 1) {
                canvas.remove("edge")
                canvas.drawNumberMarker(it)
            } else {
                canvas.drawNumberMarker(it)
                canvas.drawTrack("edge", it)
                droneCanvas.drawDistance(it, false)
            }
        }
    }

    //断点
    collectFlow(taskModel.breakWpFlow) {
        if (it == null) {
            DroneModel.bk = null
            removeBreakPoint()
        } else {
            DroneModel.bk = it//架次中的断点需要的
            drawBreakpoint(it.lat, it.lng)
        }
    }

    clearBreaksByIMU(taskModel)
    drawCalcBreaks(taskModel)
    drawWorkLine()
}