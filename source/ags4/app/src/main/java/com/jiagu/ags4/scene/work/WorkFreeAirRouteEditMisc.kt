package com.jiagu.ags4.scene.work

import com.jiagu.ags4.vm.FreeAirRouteEditModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.tools.map.IMapCanvas

//block edit
//索引点击
private class FreeAirRouteEditMarkerClickListener(
    val canvas: IMapCanvas,
    val freeAirRouteEditModel: FreeAirRouteEditModel,
) :
    IMapCanvas.MapMarkerSelectListener {
    override fun onMarkerSelect(marker: String) {
        val markerIndex = canvas.indexOfNumberMarker(marker)
        if (freeAirRouteEditModel.selectedMarkerIndex == markerIndex) {
            return
        }
        freeAirRouteEditModel.markerSelect(markerIndex)
    }
}

private class FreeAirRouteEditMapClickListener(
    val canvas: IMapCanvas,
    val freeAirRouteEditModel: FreeAirRouteEditModel,
) : IMapCanvas.MapClickListener {
    override fun onClick(pt: GeoHelper.LatLng) {
        //取消高亮marker
        unhighlightMarker(canvas)
        freeAirRouteEditModel.clearMarker()
    }
}


fun MapVideoActivity.collectFreeAirRouteEdit(
    freeAirRouteEditModel: FreeAirRouteEditModel,
    locationModel: LocationModel,
) {
    addMapChangeListener(MapChangedListener(canvas, locationModel))
    addMapClickListener(FreeAirRouteEditMapClickListener(canvas, freeAirRouteEditModel))
    addMarkClickListener(FreeAirRouteEditMarkerClickListener(canvas, freeAirRouteEditModel))
    //选择的marker点
    collectFlow(freeAirRouteEditModel.selectedMarkerIndexFlow) {
        unhighlightMarker(canvas)
        if (it >= 0) {
            highlightMarker(canvas, it)
        }
    }
    //点
    collectFlow(freeAirRouteEditModel.pointsFlow) {
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
        //fit判断，一般是导入kml时使用
        if (freeAirRouteEditModel.canvasFit) {
            canvas.fit()
            freeAirRouteEditModel.canvasFit = false
        }
    }
}