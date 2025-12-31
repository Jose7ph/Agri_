package com.jiagu.ags4.vm.task

import com.jiagu.ags4.utils.logToFile
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAgCmd

class WaterLinePumpCalibrationTask(private val pumpIndex: Int) : ProgressTask() {
    private val monitor = { data: Any ->
        if (data is IProtocol.AnswerUserData) {
            logToFile("线性水泵校准($pumpIndex)：${data}")
            if (data.msgid == VKAgCmd.MSG_CAL.toInt() && data.cmd == VKAgCmd.CAL_CALIB_LINE_PUMP.toInt()) {
                if (data.param1 == 2) {//收到2显示校准完成
                    resume(1)
                } else if (data.param1 == 3) {
                    resume(-1)
                }
            }
        }
    }

    private fun startCalibBump() {//1开始 2-结束
        DroneModel.activeDrone?.apply {
            DroneModel.activeDrone?.startDataMonitor(monitor)
            calibLinePump(1, pumpIndex = pumpIndex)
        }
    }

    private fun stopDataMonitor() {
        DroneModel.activeDrone?.stopDataMonitor(monitor)
    }

    override suspend fun start(): Pair<Boolean, String?> {
        logToFile("线性水泵校准开始($pumpIndex)...")
        startCalibBump()
        val r = await() // 等待AnswerUserData返回resume
        stopDataMonitor()
        logToFile("线性水泵校准结果($pumpIndex): ${r}")
        return if (r > 0) {
            true to null
        } else {
            false to null
        }
    }
}