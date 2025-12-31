package com.jiagu.ags4.scene.work

import android.graphics.Color
import com.jiagu.ags4.vm.BlockDivisionModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapRing
import com.jiagu.tools.map.IMapCanvas

//索引点击
class BlockDivisionMarkerClickListener(val blockDivisionModel: BlockDivisionModel) :
    IMapCanvas.MapMarkerSelectListener {
    override fun onMarkerSelect(marker: String) {
        val markerIndex = marker.substring(7).toInt()
        if (blockDivisionModel.selectedMarkerIndex == markerIndex) {
            return
        }
        blockDivisionModel.markerSelect(markerIndex)
    }
}

class BlockDivisionMapClickListener(
    val canvas: IMapCanvas,
    val blockDivisionModel: BlockDivisionModel,
) : IMapCanvas.MapClickListener {
    override fun onClick(pt: GeoHelper.LatLng) {
        //取消高亮marker
        unhighlightMarker(canvas)
        blockDivisionModel.clearMarker()
    }
}

fun MapVideoActivity.collectBlockDivision(
    blockDivisionModel: BlockDivisionModel,
    locationModel: LocationModel,
) {
    addMapChangeListener(MapChangedListener(canvas, locationModel))
    addMapClickListener(BlockDivisionMapClickListener(canvas, blockDivisionModel))
    addMarkClickListener(BlockDivisionMarkerClickListener(blockDivisionModel))
    //选择的marker点
    collectFlow(blockDivisionModel.selectedMarkerIndexFlow) {
        unhighlightMarker(canvas)
        if (it >= 0) {
            highlightMarker(canvas, it)
        }
    }
    //分割线
    collectFlow(blockDivisionModel.divisionPointsFlow) {
        drawDivisionLine(it, canvas)
    }
    //画地块和障碍物
    collectFlow(blockDivisionModel.selectedBPFlow) {
        if (it.isNotEmpty()) {
            val block = it[0]
            canvas.drawBlock(
                "block", block.boundary[0], true
            )
            drawBarriers(canvas, block.barriers)
        }
    }
    //block
    var curBlocksCount = 0
    collectFlow(blockDivisionModel.divisionBlocksFlow) { blocks ->
        if (blocks.isEmpty()) {
            for (i in 0 until curBlocksCount) {
                canvas.remove("block${i}")
            }
            return@collectFlow
        }
        var count = 0
        for (b in blocks) {
            canvas.drawBlock("block${count++}", b[0], true)
        }
        for (i in count until curBlocksCount) {
            canvas.remove("block$i")
        }
        curBlocksCount = count
    }
}

fun drawDivisionLine(points: MapRing, canvas: IMapCanvas) {
    if (points.isEmpty()) {
        canvas.remove("line")
        canvas.remove("custom_0")
        canvas.remove("custom_1")
    } else if (points.size == 1) {
        val pt1 = points[0]
        canvas.drawLetterMarker(
            "custom_0",
            pt1.latitude,
            pt1.longitude,
            "C1",
            IMapCanvas.Params.MARKER_OTHER_COLOR
        )
        canvas.remove("line")
        canvas.remove("custom_1")
    } else {
        val pt1 = points[0]
        val pt2 = points[1]
        canvas.drawLetterMarker(
            "custom_0",
            pt1.latitude,
            pt1.longitude,
            "C1",
            IMapCanvas.Params.MARKER_OTHER_COLOR
        )
        canvas.drawLetterMarker(
            "custom_1",
            pt2.latitude,
            pt2.longitude,
            "C2",
            IMapCanvas.Params.MARKER_OTHER_COLOR
        )
        canvas.drawLine("line", listOf(pt1, pt2), Color.WHITE)
    }
}