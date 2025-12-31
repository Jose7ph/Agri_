package com.jiagu.ags4.vm.task

import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressTask
import kotlinx.coroutines.delay

class EavRcConfigTask : ProgressTask() {
    private var done = false
    private var error = ""

    companion object {
        const val MAX_TIME_OUT = 20 * 1000 //最大超时时间 20s
    }

    private val observer = { name: String, value: Any ->
        if (name == "config") {
            val msg = value as String
            if (msg != "ok") {
                error = msg
            }else{
                done = true
            }
        }
    }

    override suspend fun start(): Pair<Boolean, String?> {
        DroneModel.subscribeRcState(observer)
        DroneModel.setEavRcDefaultParam()
        val current = System.currentTimeMillis()
        while (!done) {
            delay(500)
            val t = System.currentTimeMillis()
            if (t - current > MAX_TIME_OUT) {
                DroneModel.unsubscribeRcState(observer)
                return false to error
            }
        }
        DroneModel.unsubscribeRcState(observer)
        DroneModel.readControllerParam()
        return true to null
    }
}