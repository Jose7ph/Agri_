package com.jiagu.ags4.scene.work

import com.jiagu.ags4.vm.EnhancedManualModel

fun MapVideoActivity.collectEnhancedManual(enhancedManualModel: EnhancedManualModel) {

    collectFlow(enhancedManualModel.curAngle) {
        droneCanvas.setEnhanceYaw(it)
    }
}