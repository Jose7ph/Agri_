package com.jiagu.ags4.vm.task

import android.util.Log
import com.google.gson.Gson
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol

class WaterLinePumpChartTask(private val pumpIndex: Int) : ProgressTask() {
    private var linePumpData: String? = null
    private val monitor = { data: Any ->
        if (data is IProtocol.LinePumpData) {
            val msg = Gson().toJson(data)
            linePumpData = msg
            resume(1)
        }
    }

    private fun startCalibBump() {//1-线性水泵1校准曲线
        DroneModel.activeDrone?.apply {
            startDataMonitor()
            getLinePumpData(pumpIndex)
        }
    }

    private fun startDataMonitor() {
        DroneModel.activeDrone?.startDataMonitor(monitor)
    }

    private fun stopDataMonitor() {
        DroneModel.activeDrone?.stopDataMonitor(monitor)
    }

    override suspend fun start(): Pair<Boolean, String?> {
        Log.d("zhy", "水泵校准曲线开始...")
        startCalibBump()
        val r = await() // 等待AnswerUserData返回resume
        stopDataMonitor()
        Log.d("zhy", "水泵校准曲线结果 $r ...")
        return if (r > 0) {
            true to linePumpData
        } else {
            false to null
        }
    }
}