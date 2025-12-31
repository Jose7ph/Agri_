package com.jiagu.ags4.scene.work

import android.graphics.Color
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.vm.BlockModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.tools.map.IMapCanvas
import kotlinx.coroutines.delay

//block list
private class BlockMapClickListener(val canvas: IMapCanvas, val blockModel: BlockModel) :
    IMapCanvas.MapClickListener {
    override fun onClick(pt: GeoHelper.LatLng) {
        val blocks = canvas.findBlock(pt)
        blockModel.setHighlightBlocks(blocks)
    }
}

private const val CANVAS_BLOCK_JOB_NAME = "canvasBlockJob"
fun MapVideoActivity.collectBlock(blockModel: BlockModel) {
    addMapClickListener(BlockMapClickListener(canvas, blockModel))
    clearNumbers()
    //清除canvas地块
    collectFlow(blockModel.clearFlag) {
        //true则说明触发地块刷新，需要清除已绘制的地块
        if (it) {
            blockModel.clearFlag.value = false
            canvas.clear()
            cancelJob(CANVAS_BLOCK_JOB_NAME)
            drawBlocks(blockModel = blockModel)
        }
    }
    //已选择的地块显示
    collectFlow(blockModel.selectedBPFlow) {
        val name = "selected block"
        if (it.isNotEmpty()) {
            val block = it[0]
            canvas.drawBlock(
                name, block.boundary[0], true
            )
            canvas.fit(listOf(name))
            //画障碍物
            drawBarriers(canvas, block.barriers)
        } else {
            canvas.remove(name)
            //删除障碍物
            drawBarriers(canvas, emptyList())
        }
    }
    //已经高亮的地块(用于取消高亮)
    val highlightedBlocks = mutableSetOf<String>()
    //高亮地块
    collectFlow(blockModel.highlightBlocks) {
        //取消当前已经高亮的地块
        for (hlb in highlightedBlocks) {
            canvas.highlightBlock(hlb, false)
        }
        //清空已高亮的列表
        highlightedBlocks.clear()
        //高亮当前地块，并将其添加到已高亮列表中
        for (bp in it) {
            canvas.highlightBlock(bp.uniqueId(), true)
            highlightedBlocks.add(bp.uniqueId())
        }
        //canvas定位到已高亮的地块
        canvas.fit(highlightedBlocks.toList())
        //如果已高亮的地块只有一个，显示其plan
        if (it.size == 1) {
            val bp = it[0]
            drawWorkLine(bp)
            val track = bp.plan?.track
            if (track != null) {
                canvas.drawLine("plan", track, Color.RED)
            } else {
                canvas.remove("plan")
            }
        } else {
            canvas.remove("plan")
            drawWorkLine(null)
        }
    }
}

private fun MapVideoActivity.drawBlocks(blockModel: BlockModel) {
    //画地块
    collectEveryFlowPlus(blockModel.localBlocksCanvasFlow, CANVAS_BLOCK_JOB_NAME) { curBlocks ->
        for ((index, block) in curBlocks.withIndex()) {
            if ((index + 1) % 10 == 0) {
                delay(10)
            }
            when (block.blockType) {
                Block.TYPE_BLOCK -> {
                    canvas.drawBlock(
                        block.uniqueId(), block.boundary[0], false
                    )
                }
            }
        }
    }
}