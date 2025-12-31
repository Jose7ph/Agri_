package com.jiagu.ags4.vm.task

import com.jiagu.ags4.vm.DroneObject
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.model.UploadNaviData

class NaviTask(val drone: DroneObject) : ProgressTask() {

    private var pos: GeoHelper.LatLngAlt? = null
    private lateinit var param: UploadNaviData

    fun setParam(c: GeoHelper.LatLngAlt?, p: UploadNaviData) {
        pos = c
        param = p
    }

    override suspend fun start(): Pair<Boolean, String?> {
        val home = pos
        val route = param.route
        if (home != null && route != null) {
//            postProgress(getString(R.string.checking))
            val input = mutableListOf(home.latitude, home.longitude)
            for (r in route) {
                input.add(r.latitude)
                input.add(r.longitude)
            }
        }
        return startChild(RouteTask(drone, param))
    }
}