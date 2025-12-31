package com.jiagu.ags4.scene.work

import android.graphics.Color
import com.jiagu.ags4.vm.AreaCleanModel

fun MapVideoActivity.collectAreaCleanEdit(areaCleanModel: AreaCleanModel) {
    //当前点
    collectFlow(areaCleanModel.pointsFlow) {
        canvas.drawLine("clean_line", it, Color.RED)
        canvas.drawNumberMarker(it)
    }
}
