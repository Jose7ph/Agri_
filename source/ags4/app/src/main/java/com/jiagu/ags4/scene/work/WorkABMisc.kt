package com.jiagu.ags4.scene.work

import android.graphics.Color
import androidx.lifecycle.asFlow
import com.jiagu.ags4.vm.ABModel
import com.jiagu.ags4.vm.DroneModel

fun MapVideoActivity.collectAB(abModel: ABModel) {
    cancelJob("drawBlock")
    collectFlow(abModel.abLine) {
        if (it == null) {//AB点清除后，断点也清掉
            removeBreakPoint()
            canvas.remove("ab")
        } else canvas.drawLine("ab", it, Color.RED)
    }
    collectFlow(abModel.ptA) {
        if (it == null) canvas.remove("pta")
        else canvas.drawLetterMarker(
            "pta",
            it.latitude,
            it.longitude,
            "A",
            Color.argb(127, 0, 128, 0)
        )
    }
    collectFlow(abModel.ptB) {
        if (it == null) canvas.remove("ptb")
        else canvas.drawLetterMarker(
            "ptb",
            it.latitude,
            it.longitude,
            "B",
            Color.argb(127, 0, 128, 0)
        )
    }
    collectFlow(abModel.lineA) {
        if (it == null) canvas.remove("ab_a")
        else canvas.drawLine("ab_a", it, Color.YELLOW)
    }
    collectFlow(abModel.lineB) {
        if (it == null) canvas.remove("ab_b")
        else canvas.drawLine("ab_b", it, Color.YELLOW)
    }
    //飞控断点
    collectFlow(abModel.abWpFlow) {
        if (it == null) removeBreakPoint()
        else drawBreakpoint(it.break_lat, it.break_lng)
    }
    //计算的断点
    collectFlow(abModel.calcBreaks) {
        if (it.isNotEmpty()) {
            when (it.size) {
                3 -> {
                    drawABBreak(it[0].break_lat, it[0].break_lng)
                    drawABBreak1(it[1].break_lat, it[1].break_lng)
                    drawABBreak2(it[2].break_lat, it[2].break_lng)
                }

                2 -> {
                    drawABBreak(it[0].break_lat, it[0].break_lng)
                    drawABBreak1(it[1].break_lat, it[1].break_lng)
                    removeABBreak2()
                }

                1 -> {
                    drawABBreak(it[0].break_lat, it[0].break_lng)
                    removeABBreak1()
                    removeABBreak2()
                }
            }
        }else{
            removeAllCalcBreakpoint()
        }
    }

    collectFlow(DroneModel.imuData.asFlow()) {
        abModel.abWp?.let { bk ->
            if (abModel.selectBreakIndex != -1) { //选择了计算的断点
                abModel.curCalcBK?.let { curBK ->
                    if (it.target.toInt() > curBK.index + 1) {
                        removeAllCalcBreakpoint()
                        abModel.clearCalcBK()
                    }
                }
            } else {
                if (it.target.toInt() > bk.index + 1) {
                    removeBreakPoint()
                }
            }
        }
    }
}
