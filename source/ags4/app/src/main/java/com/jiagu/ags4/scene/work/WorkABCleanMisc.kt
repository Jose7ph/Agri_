package com.jiagu.ags4.scene.work

import android.graphics.Color
import com.jiagu.ags4.vm.ABCleanModel

fun MapVideoActivity.collectABClean(abCleanModel: ABCleanModel) {
    //当前点
    collectFlow(abCleanModel.pointsFlow) {
        canvas.drawLine("clean_line", it, Color.RED)
        canvas.drawNumberMarker(it)
    }
    //断点
    collectFlow(abCleanModel.breakPointFlow) {
        if (it.isNotEmpty()) {
            drawABBreak(it[0].lat, it[0].lng)
        } else {
            removeABBreak()
        }
    }
}
