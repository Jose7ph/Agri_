package com.jiagu.ags4.vm.task

import com.jiagu.ags4.vm.DroneObject
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.v9sdk.R
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.task.UploadTool

class RouteTask(val drone: DroneObject, val param: UploadNaviData) : ProgressTask() {

    override suspend fun start(): Pair<Boolean, String?> {
        return if (param.type == UploadNaviData.TYPE_NAVI) {
            postProgress(getString(R.string.work_uploading_param))
            var curIndex = 0
            var r: Pair<Boolean, String?>
            while (curIndex < param.commands.size) {
                r = startChild(UploadTool.CommandTask(drone, param.commands[curIndex]))
                if (r.first) curIndex++
            }
            r = startChild(UploadTool.NaviTask(drone, param))
            if (!r.first) return r
            if (drone.hasBP()) {
                r = startChild(UploadTool.CommandTask(drone) { drone.setBP() })
                if (!r.first) return false to getString(R.string.work_fail_upload_param)
            }
            true to null
        } else {
            startChild(UploadTool.NaviTask(drone, param))
        }
    }
}