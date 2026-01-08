package com.jiagu.ags4.ihattys.ble

import androidx.lifecycle.Observer
import com.jiagu.ags4.repo.net.model.DroneDetail
import com.jiagu.ags4.vm.DroneModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IhattysIdentityProvider {

    private val _identity = MutableStateFlow<IhattysIdentity?>(null)
    val identity: StateFlow<IhattysIdentity?> = _identity

    // ✅ DroneModel.detail -> DroneDetail -> staticInfo (DroneDevice)
    private val observer = Observer<DroneDetail?> { detail ->
        val device = detail?.staticInfo
        _identity.value = IhattysDroneModelMapper.readIdentityOrNull(device)
    }

    fun start() {
        DroneModel.detail.observeForever(observer)
    }

    fun stop() {
        DroneModel.detail.removeObserver(observer)
    }
}
