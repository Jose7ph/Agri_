package com.jiagu.ags4.vm.task

import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.WaitTask


//Created by gengmeng on 6/14/23.

class ParamAckTask(var work: () -> Unit): WaitTask() {

    private val observer = { done: Boolean ->
        if (done) resume(0)
        else resume(-1)
    }

    private fun startCommand() {
        DroneModel.activeDrone?.startMonitorCommand(observer)
    }

    private fun stopCommand() {
        DroneModel.activeDrone?.stopMonitorCommand(observer)
    }

    override fun before(): Pair<Boolean, String?> {
        startCommand()
        work()
        return true to null
    }

    override fun done(result: Int): Pair<Boolean, String?> {
        stopCommand()
        return (result == 0) to null
    }
}