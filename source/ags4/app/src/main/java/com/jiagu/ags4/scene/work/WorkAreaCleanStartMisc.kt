package com.jiagu.ags4.scene.work

import com.jiagu.ags4.vm.AreaCleanModel

fun MapVideoActivity.collectAreaCleanParam(areaCleanModel: AreaCleanModel) {
    //断点
    collectFlow(areaCleanModel.breakPointFlow) {
        if (it.isNotEmpty()) {
            drawABBreak(it[0].lat, it[0].lng)
        } else {
            removeABBreak()
        }
    }

    //已选择的地块显示 默认显示第一个
    collectFlow(areaCleanModel.selectedBPFlow) {
        val name = "selected clean block"
        if (it.isNotEmpty()) {
            val block = it[0]
            canvas.drawBlock(
                name, block.boundary[0], true
            )
            canvas.drawNumberMarker(block.boundary[0])
            canvas.fit(listOf(name))
        } else {
            droneCanvas.clearText()
            canvas.remove(name)
            canvas.drawNumberMarker(listOf())
        }
    }
}