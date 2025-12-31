package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.utils.exeTask
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.model.RtkLatLng
import kotlinx.coroutines.flow.MutableStateFlow

class LocatorModel(app: Application) : AndroidViewModel(app) {

    //单次记录距离
    var recordDistance = 2.0

    //开始记录
    var isStartRecord by mutableStateOf(false)

    //蓝牙列表弹窗
    var showBluetoothList by mutableStateOf(true)

    val locatorList = mutableStateListOf<RTKLocatorData>()
    val locatorListFlow = MutableStateFlow<List<RTKLocatorData>>(emptyList())

    fun addPoint(rtkLatLng: RtkLatLng) {
        val newPoint = RTKLocatorData(
            pt = GeoHelper.LatLngAlt(rtkLatLng.lat, rtkLatLng.lng, rtkLatLng.alt),
            level = processLevel(rtkLatLng.info.hdop)
//            level = Random.nextInt(1, 4)
        )

        val shouldAdd = when {
            locatorList.isEmpty() -> true
            else -> {
                val lastPt = locatorList.last().pt
                val (lat1, lng1) = lastPt.latitude to lastPt.longitude
                val (lat2, lng2) = newPoint.pt.latitude to newPoint.pt.longitude
                GeoHelper.distance(lat1, lng1, lat2, lng2) >= recordDistance
            }
        }

        if (shouldAdd) {
            locatorList.add(newPoint)
            pushLocatorList()
        }
    }

    fun pushLocatorList() {
        exeTask {
            locatorListFlow.emit(locatorList.toList())
        }
    }

    fun processLevel(value: Float?): Int {
        if (value == null) return 0
        return when {
            value > 5f -> 1
            value > 1f -> 2  // 包含 1 < value ≤5
            value > 0f -> 3  // 包含 0 < value ≤1
            else -> 0
        }
    }
}

data class RTKLocatorData(val pt: GeoHelper.LatLngAlt, val level: Int)