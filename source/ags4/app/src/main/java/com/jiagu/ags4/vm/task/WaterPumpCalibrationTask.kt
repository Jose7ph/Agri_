package com.jiagu.ags4.vm.task

import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd

class WaterPumpCalibrationTask() : ProgressTask() {
    private val monitor = { data: Any ->
        if (data is IProtocol.AnswerData) {
            if (data.msgid == VKAgCmd.MSG_CAL.toInt() && data.cmd == VKAgCmd.CAL_BUMP_START.toInt() && data.param1 == 0) {
                if (data.param2 == 1) resume(-1)
                else if (data.param2 == 0) resume(1)
            }
        }
        if (data is VKAg.PumpData) {
            postProgress((data.progress / 100f).toString())
        }
    }

    private fun startCalibBump() {//0开始 1-取消 0xff-恢复出场设置
        DroneModel.activeDrone?.apply {
            DroneModel.activeDrone?.startDataMonitor(monitor)
            calibBumpChart(0)
        }
    }

    private fun stopCalibBump() {//0开始 1-取消 0xff-恢复出场设置
        DroneModel.activeDrone?.apply {
            calibBumpChart(1)
            DroneModel.activeDrone?.stopDataMonitor(monitor)
        }
    }

    override suspend fun start(): Pair<Boolean, String?> {
        startCalibBump()
        val r = await() // 等待AnswerUserData返回resume
        stopCalibBump()
        return if (r > 0) {
            true to null
        } else {
            false to null
        }
    }
}