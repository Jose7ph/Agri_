package com.jiagu.ags4.vm.com.jiagu.ags4.vm.work

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jiagu.api.helper.GeoHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.cos
import kotlin.math.sin

interface IWorkMapMarker {
    var showMarkerPanel: Boolean
    var selectedMarkerIndex: Int
    var selectedMarkerIndexFlow: MutableStateFlow<Int>
    var markerInfo: MarkerInfo?
    var defaultDistStep: Double
    var moveDist: Double
    var markerRadius: Double
    fun changeSelectedMarkerIndex(index: Int)
    fun clearMarker()
    fun initMarker(position: GeoHelper.LatLngAlt, radius: Double = 0.0)
    fun resetMarkerDist(): GeoHelper.LatLngAlt?
    fun moveWest(angle: Float): GeoHelper.LatLngAlt?
    fun moveEast(angle: Float): GeoHelper.LatLngAlt?
    fun moveNorth(angle: Float): GeoHelper.LatLngAlt?
    fun moveSouth(angle: Float): GeoHelper.LatLngAlt?
    fun moveWestNorth(angle: Float): GeoHelper.LatLngAlt?
    fun moveWestSouth(angle: Float): GeoHelper.LatLngAlt?
    fun moveEastNorth(angle: Float): GeoHelper.LatLngAlt?
    fun moveEastSouth(angle: Float): GeoHelper.LatLngAlt?
    fun moveMarker(distX: Double, distY: Double, angle: Float): GeoHelper.LatLngAlt?
    fun calcDelta(
        distX: Double,
        distY: Double,
        lat: Double,
        lng: Double,
        angle: Float,
    ): Pair<Double, Double>
}

class WorkMapMarkerImpl : IWorkMapMarker {
    override var showMarkerPanel by mutableStateOf(false) //航点移动弹窗显示控制
    override var selectedMarkerIndex by mutableIntStateOf(-1)//当前marker索引
    override var selectedMarkerIndexFlow = MutableStateFlow(-1)//当前marker索引
    override var markerInfo by mutableStateOf<MarkerInfo?>(null) //当前marker信息
    override var defaultDistStep by mutableDoubleStateOf(1.0)
    override var moveDist by mutableDoubleStateOf(0.0)
    override var markerRadius by mutableDoubleStateOf(0.0)

    var tempMarkerInfo: MarkerInfo? = null

    override fun changeSelectedMarkerIndex(index: Int) {
        selectedMarkerIndex = index
        selectedMarkerIndexFlow.value = index
    }

    override fun clearMarker() {
        selectedMarkerIndex = -1
        selectedMarkerIndexFlow.value = -1
        showMarkerPanel = false
        tempMarkerInfo = null
        markerInfo = null
        resetMarkerDist()
    }

    override fun initMarker(position: GeoHelper.LatLngAlt, radius: Double) {
        markerInfo = MarkerInfo(
            lat = position.latitude,
            lng = position.longitude,
            alt = position.altitude,
            index = selectedMarkerIndex,
        )
        tempMarkerInfo = markerInfo
        markerRadius = radius
        resetMarkerDist()
    }

    override fun resetMarkerDist(): GeoHelper.LatLngAlt? {
        moveDist = 0.0
        markerInfo?.let {
            tempMarkerInfo = it
            return GeoHelper.LatLngAlt(it.lat, it.lng, it.alt)
        }
        return null
    }

    override fun moveWest(angle: Float): GeoHelper.LatLngAlt? { //西
        return moveMarker(distX = -defaultDistStep, distY = 0.0, angle = angle)
    }

    override fun moveEast(angle: Float): GeoHelper.LatLngAlt? { //东
        return moveMarker(distX = defaultDistStep, distY = 0.0, angle = angle)
    }

    override fun moveNorth(angle: Float): GeoHelper.LatLngAlt? { //北
        return moveMarker(distX = 0.0, distY = defaultDistStep, angle = angle)
    }

    override fun moveSouth(angle: Float): GeoHelper.LatLngAlt? { //南
        return moveMarker(distX = 0.0, distY = -defaultDistStep, angle = angle)
    }

    override fun moveWestNorth(angle: Float): GeoHelper.LatLngAlt? { //西北
        return moveMarker(distX = -defaultDistStep, distY = defaultDistStep, angle = angle)
    }

    override fun moveWestSouth(angle: Float): GeoHelper.LatLngAlt? { //西南
        return moveMarker(distX = -defaultDistStep, distY = -defaultDistStep, angle = angle)
    }

    override fun moveEastNorth(angle: Float): GeoHelper.LatLngAlt? { //东北
        return moveMarker(distX = defaultDistStep, distY = defaultDistStep, angle = angle)
    }

    override fun moveEastSouth(angle: Float): GeoHelper.LatLngAlt? { //东南
        return moveMarker(distX = defaultDistStep, distY = -defaultDistStep, angle = angle)
    }

    override fun moveMarker(distX: Double, distY: Double, angle: Float): GeoHelper.LatLngAlt? {
        tempMarkerInfo?.let {
            val (dLat, dLng) = calcDelta(distX, distY, it.lat, it.lng, angle)
            val nLat = dLat + it.lat
            val nLng = dLng + it.lng
            calcMoveDist(lat = markerInfo!!.lat, lng = markerInfo!!.lng, nLat = nLat, nLng = nLng)
            tempMarkerInfo = MarkerInfo(
                lat = nLat,
                lng = nLng,
                alt = it.alt,
                index = it.index,
            )
            return GeoHelper.LatLngAlt(dLat, dLng, it.alt)
        }
        return null
    }

    fun calcMoveDist(lat: Double, lng: Double, nLat: Double, nLng: Double) {
        val converter = GeoHelper.GeoCoordConverter(lat, lng)
        val pts = converter.convertLatLng(nLat, nLng)
        moveDist = converter.convertLatLng(lat, lng).distance(pts)
    }

    override fun calcDelta(
        distX: Double,
        distY: Double,
        lat: Double,
        lng: Double,
        angle: Float,
    ): Pair<Double, Double> {
        val converter = GeoHelper.GeoCoordConverter(lat, lng)
        val rad = -Math.toRadians(angle.toDouble())
        val sinx = sin(rad)
        val cosx = cos(rad)
        val x = distX * cosx - distY * sinx
        val y = distX * sinx + distY * cosx
        val delta = converter.convertPoint(x, y)
        return (delta.latitude - lat) to (delta.longitude - lng)
    }
}

data class MarkerInfo(
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var alt: Double = 0.0,
    val index: Int = 0,
)
