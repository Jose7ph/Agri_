package com.jiagu.ags4.vm.task

import com.jiagu.ags4.utils.logToFile
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAgCmd

class WaterLinePumpConfigTask : ProgressTask() {
    private val monitor = { data: Any ->
        if (data is IProtocol.AnswerUserData) {
            logToFile("线性水泵配置 $data")
            if (data.msgid == VKAgCmd.MSG_CAL.toInt() &&
                data.cmd == VKAgCmd.CAL_CONFIG_LINE_PUMP.toInt()) {
                if (data.param2 == 1) {//校准完成
                    resume(data.param1)
                }
            }
        }
    }

    private fun startConfigLineBump() {//配置线性水泵
        DroneModel.activeDrone?.apply {
            startDataMonitor()
            configLinePump()
        }
    }

    private fun startDataMonitor() {
        DroneModel.activeDrone?.startDataMonitor(monitor)
    }

    private fun stopDataMonitor() {
        DroneModel.activeDrone?.stopDataMonitor(monitor)
    }

    override suspend fun start(): Pair<Boolean, String?> {
        logToFile("配置线性水泵配置开始...")
        startConfigLineBump()
        val r = await() // 等待AnswerUserData返回resume
        stopDataMonitor()
        logToFile("配置线性水泵配置结果 $r")
        return true to "$r"
    }
}