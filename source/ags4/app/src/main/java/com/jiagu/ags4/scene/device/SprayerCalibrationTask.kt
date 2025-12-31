package com.jiagu.ags4.scene.device

import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAgCmd
import kotlinx.coroutines.withTimeoutOrNull

/**
 *去皮校准
 */
class SprayerCalibrationTask(val type: Int, vararg params: Int, val kValueNum: Int? = null) :
    ProgressTask() {
    val values = params

    companion object {
        const val ID_ZERO = 1
        const val ID_WEIGHT = 2
        const val ID_RESET = 3
        const val ID_WEIGHT_K = 4
        const val ID_FLOW = 5
        const val ID_FLOW_RESET = 6
        const val ID_FLOW_CLEAR_BG = 7
    }

    private var currentCmdMsgId = when (type) {
        ID_ZERO -> VKAgCmd.CAL_ZERO
        ID_WEIGHT -> VKAgCmd.CAL_WEIGHT
        ID_RESET -> -1
        ID_WEIGHT_K -> VKAgCmd.CAL_WEIGHT_K
        ID_FLOW -> VKAgCmd.CAL_FLOW1
        ID_FLOW_RESET -> VKAgCmd.CAL_FLOW2
        ID_FLOW_CLEAR_BG -> VKAgCmd.CAL_FLOW2
        else -> -1
    }

    private val monitor = { data: Any ->
        if (data is IProtocol.AnswerUserData &&
            data.msgid == VKAgCmd.MSG_CAL.toInt() && currentCmdMsgId.toInt() == data.cmd) {
            when (type) {
                ID_FLOW_RESET -> {
                    resume(if (data.param2 == 0) 1 else -1)
                }
                ID_FLOW_CLEAR_BG -> {
                    if (data.param1 == 0xA) {
                        resume(if (data.param2 == 0) 1 else -1)
                    }
                }
                else -> resume(1)
            }
        }
    }

    fun cancel(): Boolean {
        DroneModel.activeDrone?.stopDataMonitor(monitor)
        return true
    }

    private fun setCalibZero() {
        DroneModel.activeDrone?.apply {
            when (type) {
                ID_ZERO -> calibSeeder(currentCmdMsgId)
                ID_WEIGHT -> calibSeeder(currentCmdMsgId, values[0])
                ID_WEIGHT_K -> {
                    if (kValueNum == null || kValueNum < 0) return
                    setWeightK(currentCmdMsgId, getKValue(kValueNum))
                }
                ID_FLOW -> {
                    if (values[0] == -1 || values[1] == -1) return
                    calibEFTFlow(values[0], values[1])
                }
                ID_FLOW_RESET -> calibSeeder(currentCmdMsgId, 0xff)
                ID_FLOW_CLEAR_BG -> clearBgFlow()
            }
        }
    }

    suspend fun tryCalib(): Int {
        var result = 0
        for (i in 0..5) { // retry 6 times
            result = withTimeoutOrNull(1000) {
                setCalibZero()
                await()
            } ?: -100
            if (result != -100) break
        }
        return result
    }

    override suspend fun start(): Pair<Boolean, String?> {
        //k value校准返回对应的number
        val str = when (type) {
            ID_WEIGHT_K -> kValueNum.toString()
            else -> null
        }

        DroneModel.activeDrone?.startDataMonitor(monitor)
        DroneModel.activeDrone?.getParameters()
        val res = tryCalib()
        DroneModel.activeDrone?.stopDataMonitor(monitor)
        return (res > 0) to str
    }

    private fun getKValue(num: Int): Int {
        var v = -1
        try {
            val k = values[0]
            v = num or (k shl 4)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return v
    }
}

