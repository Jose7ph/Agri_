package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.utils.exeTask
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.locator.ILocator
import com.jiagu.device.locator.Locator
import com.jiagu.device.model.LocationInfo
import com.jiagu.device.model.RtkLatLng
import com.jiagu.device.rtcm.IRtcmProvider
import com.jiagu.device.vkprotocol.RtkStation
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.tools.vm.PhoneLocationLiveData
import kotlinx.coroutines.flow.MutableStateFlow

val LocalLocationModel = compositionLocalOf<LocationModel> {
    error("No LocationModel provided")
}

class LocationModel(app: Application) : AndroidViewModel(app),
    RtkStation.RtkStationListener, IRtcmProvider.RtcmSubscriber {

    override fun receiveRtcmData(rtcm: ByteArray) {
        locator?.sendRTCM(rtcm)
    }

    override fun onConnect(status: Boolean) {
        locConnect.postValue(status)
    }

    override fun onLocatorId(id: String?) {}

    override fun onLocation(pos: RtkLatLng) {
        exeTask { location.emit(pos) }
        if (pos.type > 0) {
            RtcmModel.sendPosition(pos.lat, pos.lng, pos.alt, pos.info.locType ?: 1, pos.info.svNum ?: 10)
        }
    }

    override fun onVersion(version: VKAg.VERData) {}
    override fun onRTCMData(rtcm: ByteArray) {}
    override fun onStationData(data: Any) {}

    val location = MutableStateFlow<RtkLatLng?>(null)
    val locConnect = MutableLiveData<Boolean>()
    var mapCenter = MutableLiveData<GeoHelper.LatLng>()

    private val config = Config(getApplication())
    private var locator: ILocator? = null
    private var data: LiveData<RtkLatLng>? = null
    private val locationObserver = Observer<RtkLatLng> { exeTask { location.emit(it) } }

    override fun onCleared() {
        super.onCleared()
        remove()
    }

    fun setup() {
        data?.removeObserver(locationObserver)
        locator?.disconnect()
        locator = null

        exeTask { location.emit(null) }
        val type = config.locationType
        data = when (type) {
            "phone" -> PhoneLocationLiveData.get(getApplication(), true, config.phoneAccuracy)
            "drone" -> DroneModel.imuData.map { it.toRtkLatLng() }
            "bs" -> {
                if (config.rtkBase.contains(":")) {
                    locator = RtkStation(config.rtkBase, this, true)
                }
                null
            }
            "locator" -> {
                if (config.locator.isNotBlank()) {
                    locator = getLocator(config.locator)
                }
                null
            }
            "map" -> mapCenter.map { RtkLatLng(it.latitude, it.longitude, 0.0, 0, LocationInfo(null, null, null, null)) }
            else -> null
        }
        data?.observeForever(locationObserver)
    }

    private fun getLocator(data: String): ILocator? {
        val s = data.split("*")
        return when (s[0]) {
            "skydroid" -> Locator(s[1], this)
            else -> null
        }
    }

    fun remove(){
        data?.removeObserver(locationObserver)
        locator?.disconnect()
        RtcmModel.unsubscribeRtcm(this)
    }
}