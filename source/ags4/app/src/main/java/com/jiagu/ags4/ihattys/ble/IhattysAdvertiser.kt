package com.jiagu.ags4.ihattys.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log

class IhattysAdvertiser(
    private val context: Context,
    private val manufacturerId: Int = 2218
) {
    companion object { private const val TAG = "IHATTYS_ADV" }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback: AdvertiseCallback? = null

    fun start(payload: ByteArray, serviceUuid: ParcelUuid): Boolean {
        stop()

        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter = manager.adapter ?: return false
        if (!adapter.isEnabled) return false
        if (!adapter.isMultipleAdvertisementSupported) {
            Log.e(TAG, "MultipleAdvertisement not supported")
            return false
        }

        advertiser = adapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true) // connectable so IHATTYS app can read GATT
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(serviceUuid)
            .addManufacturerData(manufacturerId, payload)
            .build()

        val cb = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.i(TAG, "Advertising started")
            }
            override fun onStartFailure(errorCode: Int) {
                Log.e(TAG, "Advertising failed code=$errorCode")
            }
        }

        callback = cb
        advertiser?.startAdvertising(settings, data, cb)
        return true
    }

    fun stop() {
        try {
            val cb = callback
            if (cb != null) advertiser?.stopAdvertising(cb)
        } catch (_: Throwable) {}
        callback = null
        advertiser = null
    }
}
