package com.jiagu.ags4.vm.task

import com.google.gson.Gson
import com.jiagu.ags4.scene.factory.regularValue
import com.jiagu.ags4.scene.factory.rockerDataProcess
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.VKAg

/**
 * 遥控器校准
 */
class RCCalibrationTask(controllerType: String) : ProgressTask() {

    private var isCalib = false

    private class Ch(var count: Int, var state: Int, var middle: Int) {
        override fun toString(): String {
            return String.format("%3d (c:%d)-(s:%s)", middle, count, state.toString())
        }
    }

    private val calcSignal = List(4) { Ch(count = 0, middle = 150, state = 1) }

    private val monitor = { data: Any ->
        when (data) {
            is VKAg.PWMData -> {
                val ch = floatArrayOf(0f, 0f, 0f, 0f)
                LogFileHelper.log("ControllerInput value -> ${data.ControllerInput}")
                for (i in 0 until 4) {
                    ch[i] = regularValue(data.ControllerInput[i])
                }
                // up/down have to reverse
                ch[2] = -ch[2]
                LogFileHelper.log("rocker pwdData -> ${ch.contentToString()}")
                //处理摇杆数据
                val rockerData = rockerDataProcess(ch, controllerType)
                LogFileHelper.log("rocker data process -> ${rockerData.contentToString()}")
                postProgress(Gson().toJson(rockerData))
                calcChSuccess(data.ControllerInput)
            }
        }
    }

    private fun calcChSuccess(ch: ShortArray?) {
        if (!isCalib) return
        if (ch == null || ch.size < 4) {
            return
        }
        LogFileHelper.log("calc ch start....")
        for (i in 0 until 4) {
            var desc = "通道${i + 1}"
            val signal = calcSignal[i]
            val ch1 = ch[i].toInt()
            LogFileHelper.log("ch1:${ch1},state:${signal.state}")
            when (signal.state) {
                1 -> {
                    if (ch1 > signal.middle && ch1 > 165) {//当前值比中间值大
                        signal.state = 2
                        signal.count += 1
                        desc += " 中->大的过程"
                        LogFileHelper.log("state1:[$desc] -> ch1 > signal.middle && ch1 > 165")
                    }
                    if (ch1 < signal.middle && ch1 < 135) {//当前值比中间值小
                        signal.state = 3
                        signal.count += 1
                        desc += " 中->小的过程"
                        LogFileHelper.log("state1:[$desc] -> ch1 < signal.middle && ch1 < 135")
                    }
                }

                2 -> {
                    if (ch1 < signal.middle && ch1 < 135) {//当前值比中间值小
                        signal.state = 1
                        signal.count += 1
                        desc += " 完成一个大->小的过程"
                        LogFileHelper.log("state2:[$desc] -> ch1 < signal.middle && ch1 < 135")
                    }
                }

                3 -> {
                    if (ch1 > signal.middle && ch1 > 165) {//当前值比中间值大
                        signal.state = 1
                        signal.count += 1
                        desc += " 完成一个小->大的过程"
                        LogFileHelper.log("state3:[$desc] -> ch1 > signal.middle && ch1 > 165")
                    }
                }
            }
        }
        val signal = calcSignal.all { it.count >= 4 }
        LogFileHelper.log("current signal count : $signal")
        if (signal) {
            var c = true
            for (i in 0 until 4) {
                if (ch[i] !in 140..160) {
                    LogFileHelper.log("ch[$i] !in 140..160, current value :${ch[i]}")
                    c = false
                }
            }
            //成功
            if (c) {
                resume(1)
            }
        }
    }

    override suspend fun start(): Pair<Boolean, String?> {
        isCalib = true
        calcSignal.forEach { it.count = 0; it.middle = 150; it.state = 1; }
        DroneModel.activeDrone?.startDataMonitor(monitor)
        DroneModel.activeDrone?.calibController()
        val r = await()
        if (r == -1) {
            DroneModel.activeDrone?.stopCalibController(0)
            DroneModel.activeDrone?.stopDataMonitor(monitor)
            isCalib = false
            return false to "取消了校准"
        }
        //取消校准
        DroneModel.activeDrone?.stopCalibController(0)
        DroneModel.activeDrone?.stopDataMonitor(monitor)
        isCalib = false
        return true to null
    }
}