package com.example.drone.server

import com.example.drone.GpsInfo
import com.example.drone.GpsSignalLevel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class FlightControllerMonitor {

    private val gpsInfoRef = AtomicReference(
        GpsInfo.newBuilder()
            .setNumSatellites(0)
            .setSignalLevel(GpsSignalLevel.LEVEL_0)
            .build()
    )

    private val gpsListeners = CopyOnWriteArrayList<(GpsInfo) -> Unit>()

    fun getGpsInfo(): GpsInfo = gpsInfoRef.get()

    fun setGpsInfo(info: GpsInfo) {
        gpsInfoRef.set(info)
        gpsListeners.forEach { it(info) }
    }

    fun onGps(listener: (GpsInfo) -> Unit) = gpsListeners.add(listener)
    fun offGps(listener: (GpsInfo) -> Unit) = gpsListeners.remove(listener)
}
