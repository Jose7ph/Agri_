package com.jiagu.ags4.scene.work

import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.FreeAirRouteParamModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.tools.map.IMapCanvas

//block edit
//索引点击
class FreeAirRouteParamMarkerClickListener(
    val canvas: IMapCanvas,
    val freeAirRouteParamModel: FreeAirRouteParamModel,
) : IMapCanvas.MapMarkerSelectListener {
    override fun onMarkerSelect(marker: String) {
        val markerIndex = canvas.indexOfNumberMarker(marker)
        if (freeAirRouteParamModel.selectedMarkerIndex == markerIndex) {
            return
        }
        freeAirRouteParamModel.changeSelectedMarkerIndex(markerIndex)
    }
}

class FreeAirRouteParamMapClickListener(
    val canvas: IMapCanvas,
    val freeAirRouteParamModel: FreeAirRouteParamModel,
) : IMapCanvas.MapClickListener {
    override fun onClick(pt: GeoHelper.LatLng) {
        //取消高亮marker
        unhighlightMarker(canvas)
        freeAirRouteParamModel.clearMarker()
    }
}

fun MapVideoActivity.collectFreeAirRouteParam(
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

    collectFlow(taskModel.breakWpFlow) {
        if (it == null) {
            DroneModel.bk = null
            removeBreakPoint()
        } else {
            DroneModel.bk = it//架次中的断点需要的
            drawBreakpoint(it.lat, it.lng)
        }
    }

    //已作业轨迹 编辑参数进入相当于新规划不显示已作业轨迹
    drawWorkLine()
}