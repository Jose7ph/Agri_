package com.jiagu.ags4.scene.work.settings

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.jiagu.ags4.MapActivity
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.tools.map.IMapCanvas

class LocationActivity : MapActivity() {

    class PositionSettingsListener : IMapCanvas.MapClickListener {
        override fun onClick(pt: GeoHelper.LatLng) {
            DroneModel.activeDrone?.startSimulator(pt.latitude, pt.longitude)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectPosition()
        addObserver()
    }

    private fun collectPosition(){
        addMapClickListener(PositionSettingsListener())
    }

    @Composable
    override fun ContentPage() {
        PositionSettings(moveToDronePosition = { moveToDronePosition() }) { finish() }
    }

    private var firstReceiveDronePosition = false
    private var droneLocation: GeoHelper.LatLng? = null
    private fun addObserver() {
        DroneModel.imuData.observe(this) {
            droneLocation = GeoHelper.LatLng(it.lat, it.lng)
            if (it.GPSStatus > 1) {
                droneCanvas.setDronePosition(GeoHelper.LatLng(it.lat, it.lng))
                if (!firstReceiveDronePosition) {
                    firstReceiveDronePosition = true
                    canvas.moveMap(it.lat, it.lng, 15f)
                }
                droneCanvas.setProperties(it.yaw, it.GPSStatus)
                if (VKAgTool.isGoHomeMode(it.flyMode.toInt())) {
                    droneCanvas.setDashLine()
                } else {
                    droneCanvas.setDashLine()
                }
            } else {
                droneCanvas.clear()
            }
        }
    }

    private fun moveToDronePosition(){
        droneLocation?.apply { canvas.moveMap(latitude, longitude) }
    }
}