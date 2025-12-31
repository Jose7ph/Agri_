package com.jiagu.ags4.scene.mine

import com.jiagu.ags4.scene.work.BlockDivisionMapClickListener
import com.jiagu.ags4.scene.work.BlockDivisionMarkerClickListener
import com.jiagu.ags4.scene.work.BlockEditMapClickListener
import com.jiagu.ags4.scene.work.BlockEditMarkerClickListener
import com.jiagu.ags4.scene.work.MapChangedListener
import com.jiagu.ags4.scene.work.PointTypeEnum
import com.jiagu.ags4.scene.work.drawBarriers
import com.jiagu.ags4.scene.work.drawDivisionLine
import com.jiagu.ags4.scene.work.drawPoles
import com.jiagu.ags4.scene.work.highlightMarker
import com.jiagu.ags4.scene.work.unhighlightMarker


fun BlockDetailActivity.collectBlockEdit() {
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
    }
    //边界点
    collectFlow(blockEditModel.edgePointsFlow) {
        if (it.isEmpty()) canvas.remove("boundary")
        else {
            canvas.drawBlock("boundary", it, false)
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


fun BlockDetailActivity.collectBlockDivision() {
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