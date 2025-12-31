package com.jiagu.ags4.scene.work

import com.jiagu.ags4.vm.BlockEditModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapRing3D
import com.jiagu.api.model.centerOfMapRing
import com.jiagu.tools.map.DroneView
import com.jiagu.tools.map.IMapCanvas

//block edit
//索引点击
class BlockEditMarkerClickListener(
    val canvas: IMapCanvas,
    val blockEditModel: BlockEditModel,
) :
    IMapCanvas.MapMarkerSelectListener {
    override fun onMarkerSelect(marker: String) {
        val markerIndex = canvas.indexOfNumberMarker(marker)
        if (blockEditModel.selectedMarkerIndex == markerIndex) {
            return
        }
        blockEditModel.markerSelect(markerIndex)
    }
}

class BlockEditMapClickListener(
    val canvas: IMapCanvas,
    val blockEditModel: BlockEditModel,
) : IMapCanvas.MapClickListener {
    override fun onClick(pt: GeoHelper.LatLng) {
        //取消高亮marker
        unhighlightMarker(canvas)
        blockEditModel.clickMapBlock(canvas.findBlock(pt))
    }
}

fun MapVideoActivity.collectBlockEdit(
    blockEditModel: BlockEditModel,
    locationModel: LocationModel,
) {
    addMapChangeListener(MapChangedListener(canvas, locationModel))
    addMapClickListener(BlockEditMapClickListener(canvas, blockEditModel))
    addMarkClickListener(BlockEditMarkerClickListener(canvas, blockEditModel))
    //选择的marker点
    collectFlow(blockEditModel.selectedMarkerIndexFlow) {
        unhighlightMarker(canvas)
        if (it >= 0) {
            highlightMarker(canvas, it)
        }
    }
    //高亮线
    collectFlow(blockEditModel.lineFlow) {
        droneCanvas.clearText()
        if (it.isEmpty()) {
            canvas.remove("edge")
            clearNumbers()
        } else {
            if (it.size == 1) {
                canvas.remove("edge")
                canvas.drawNumberMarker(it)
            } else {
                canvas.drawNumberMarker(it)
                if (blockEditModel.pointType != PointTypeEnum.CIRCLE_OBSTACLE) {
                    canvas.drawEdge("edge", it)
                    droneCanvas.drawDistance(it, true)
                }
            }
        }
        //fit判断，一般是导入kml时使用
        if (blockEditModel.canvasFit) {
            canvas.fit()
            blockEditModel.canvasFit = false
        }
    }
    //边界点
    collectFlow(blockEditModel.edgePointsFlow) {
        if (it.isEmpty()) canvas.remove("boundary")
        else {
            canvas.drawBlock("boundary", it, true)
        }
    }
    //障碍点
    collectFlow(blockEditModel.obstaclePointsFlow) {
        drawBarriers(canvas, it)
    }
    //圆形障碍点
    collectFlow(blockEditModel.circleObstaclePointsFlow) {
        drawPoles(canvas, droneCanvas, it, blockEditModel)
    }
}

private var postNum = 0
fun drawPoles(
    canvas: IMapCanvas,
    droneCanvas: DroneView,
    posts: List<MapRing3D>,
    blockEditModel: BlockEditModel,
) {
    var index = 0
    var marker = false
    val centerList = mutableListOf<GeoHelper.LatLng>()
    for ((_, post) in posts.withIndex()) {
        val name = "post$index"
        if (blockEditModel.pointType != PointTypeEnum.CIRCLE_OBSTACLE) {
            canvas.drawBarrier(name, post, false)
        } else {
            droneCanvas.clearText()
            canvas.drawBarrier(name, post, true)
            val center = centerOfMapRing(post)
            centerList.add(center)
            marker = true
        }
        canvas.drawNumberMarker(centerList)
        index++
    }
    if (!marker && blockEditModel.pointType == PointTypeEnum.CIRCLE_OBSTACLE) {
        canvas.drawNumberMarker(listOf())
    }
    for (i in index until postNum) {
        canvas.remove("post$i")
    }
    postNum = index
}