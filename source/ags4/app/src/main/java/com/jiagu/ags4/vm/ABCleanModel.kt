package com.jiagu.ags4.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.task.NaviTask
import com.jiagu.ags4.vm.work.IWorkBreakpoint
import com.jiagu.ags4.vm.work.IWorkClean
import com.jiagu.ags4.vm.work.IWorkEditCanvas
import com.jiagu.ags4.vm.work.WorkBreakpointImpl
import com.jiagu.ags4.vm.work.WorkCleanImpl
import com.jiagu.ags4.vm.work.WorkEditCanvasImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.tools.v9sdk.CleanUtils

class ABCleanModel(app: Application) : AndroidViewModel(app),
    IWorkClean by WorkCleanImpl(),
    IWorkEditCanvas by WorkEditCanvasImpl(),
    IWorkBreakpoint by WorkBreakpointImpl() {
    var dronePosition: GeoHelper.LatLngAlt? = null

    private var preTime = System.currentTimeMillis()
    fun processImuData(imuData: VKAg.IMUData) {
        val current = System.currentTimeMillis()
        //飞机位置
        dronePosition = GeoHelper.LatLngAlt(imuData.lat, imuData.lng, imuData.height.toDouble())
        if (current - preTime > 500) {
            preTime = current
            val bk = breakPoint?.let {
                GeoHelper.LatLngAlt(it.lat, it.lng, it.height.toDouble())
            }
            CleanUtils.generateDroneAndBreakPoint2D(dronePosition!!, bk) { droneP, breakP ->
                dronePoint2D = droneP
                breakPoint2D = breakP
            }
        }
        checkCleanModel(imuData.flyMode.toInt())
        checkNaviDone(imuData)
        checkAndClearBreakpoint(imuData)
    }

    fun removeLastPoint() {
        exeTask {
            clearLastPoint()
            if (points.isEmpty()) {
                clearWorkPoints()
            }
        }
    }

    //当飞机飞过断点后，把地图上的断点都清除
    private fun checkAndClearBreakpoint(imuData: VKAg.IMUData) {
        breakPoint?.let { bk ->
            if (imuData.target.toInt() > bk.index + 1) {
                // 已经过了断点
                clearBreakpoint()
            }
        }
    }

    fun clearBreakpoint() {
        exeTask {
            clearBK()
        }
    }

    fun genPointAndCalc() {
        if (points.size == 2) {
            val pa = points[0]
            val pb = points[1]
            val newList = mutableListOf<GeoHelper.LatLngAlt>()
            val pc = GeoHelper.LatLngAlt(pb.latitude, pb.longitude, 1.0)
            val pd = GeoHelper.LatLngAlt(pa.latitude, pa.longitude, 1.0)
            newList.add(pa)
            newList.add(pb)
            newList.add(pc)
            newList.add(pd)
            CleanUtils.verticalAB(
                newList,
                width,
                yaw = routeYaw ?: 0f,
                isABModel = true,
            ) { boundary, pt2ds, routes ->
                exeTask {
                    calcRoutePoint(
                        boundary = boundary,
                        pts = pt2ds,
                        routes = routes,
                    )
                }
            }
        }
    }

    fun uploadNavi(): NaviTask? {
        done = false
        return uploadNavi(isABMode = true, breakPoint = this.breakPoint)
    }

    private var done = false
    private fun checkNaviDone(imuData: VKAg.IMUData) {
        if (repeatRoutes.isEmpty() || done) return
        val wt = repeatRoutes
        if ((imuData.flyMode == VKAgCmd.FLYSTATUS_GCSFANHANG && imuData.returnReason == VKAgCmd.GOHOME_REASON_HANGXIANWANCHENG && imuData.target >= wt.size) ||//19 & 17
            (imuData.flyMode == VKAgCmd.FLYSTATUS_GCSXUANTING && imuData.hoverReason == VKAgCmd.HOVER_REASON_HANGXIANWANCHENG && imuData.target >= wt.size) ||//18 & 3
            (imuData.target > wt.size)
        ) {
            done = true
            //完成后清除断点
            clearBreakpoint()
        }
    }
}