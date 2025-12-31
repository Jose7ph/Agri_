package com.jiagu.ags4.scene.work

import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.vm.FreeAirRouteModel
import com.jiagu.tools.map.IMapCanvas
import kotlinx.coroutines.delay

private const val CANVAS_FREE_AIR_ROUTE_JOB_NAME = "canvasFreeAirRouteJob"
fun MapVideoActivity.collectFreeAirRoute(freeAirRouteModel: FreeAirRouteModel) {
    //清除canvas地块
    collectFlow(freeAirRouteModel.clearFlag) {
        //true则说明触发地块刷新，需要清除已绘制的地块
        if (it) {
            freeAirRouteModel.clearFlag.value = false
            canvas.clear()
            cancelJob(CANVAS_FREE_AIR_ROUTE_JOB_NAME)
            drawTrack(freeAirRouteModel)
        }
    }
    //自由航线只会有1个高亮地块
    //已经高亮的地块(用于取消高亮)
    var curHighlight: BlockPlan? = null
    //高亮地块
    collectFlow(freeAirRouteModel.highlightBlocks) { hlbs ->
        //先取消高亮
        curHighlight?.let {
            canvas.drawLine(
                it.uniqueId(), it.boundary[0],
                color = IMapCanvas.Params.TRACK_COLOR,
                z = IMapCanvas.Z_HL_LINE,
                width = IMapCanvas.Params.TRACK_WIDTH
            )
        }
        if (hlbs.isNotEmpty()) {
            curHighlight = hlbs[0]
            curHighlight?.let {
                canvas.drawLine(
                    it.uniqueId(), it.boundary[0],
                    color = IMapCanvas.Params.BLOCK_FILL_COLOR,
                    z = IMapCanvas.Z_HL_LINE,
                    width = IMapCanvas.Params.COMPLETION_WIDTH
                )
                canvas.fit(listOf(it.uniqueId()))
            }
        }
    }
}

private fun MapVideoActivity.drawTrack(freeAirRouteModel: FreeAirRouteModel) {
    //画航线
    collectEveryFlowPlus(
        freeAirRouteModel.localBlocksCanvasFlow,
        CANVAS_FREE_AIR_ROUTE_JOB_NAME
    ) { curBlocks ->
        for ((index, block) in curBlocks.withIndex()) {
            if ((index + 1) % 10 == 0) {
                delay(10)
            }
            when (block.blockType) {
                Block.TYPE_TRACK -> canvas.drawTrack(block.uniqueId(), block.boundary[0])
            }
        }
    }
}