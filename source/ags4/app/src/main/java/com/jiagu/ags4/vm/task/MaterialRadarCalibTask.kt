package com.jiagu.ags4.vm.task

import android.util.Log
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import kotlinx.coroutines.withTimeoutOrNull

class MaterialRadarCalibTask() : ProgressTask() {
    private val cmdId = VKAgCmd.CAL_CALIB_MATERIAL_RADAR.toInt()


    private val monitor = { data: Any ->
        if (data is IProtocol.AnswerUserData) {
            if (data.msgid == VKAgCmd.MSG_CAL.toInt() && data.cmd == cmdId) {
                if (data.param1 == 1) {
                    Log.d("zhy", "calibMaterialRadar success")
                    postProgress("100")
                    resume(1)
                }else {
                    resume(-1)
                }

            }
        }
        if (data is VKAg.PumpData) {
            Log.d("zhy", "PumpData progress value :${data.progress / 100f} ")
            postProgress((data.progress / 100f).toString())
        }
    }

    private fun startDataMonitor() {
        DroneModel.activeDrone?.apply {
            startDataMonitor(monitor)
            calibMaterialRadar(1)
        }
    }

    private fun stopDataMonitor() {//UserData
        DroneModel.activeDrone?.apply {
            stopDataMonitor(monitor)
            calibMaterialRadar(2)
        }
    }

    override suspend fun start(): Pair<Boolean, String?> {
        startDataMonitor()
        val r = withTimeoutOrNull(5_000L) {
            await()
        } ?: -1
        stopDataMonitor()
        return if (r > 0) {
            true to ""
        } else {
            false to null
        }
    }
}