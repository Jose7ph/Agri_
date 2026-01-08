package com.jiagu.ags4.ihattys.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.UUID

class IhattysGattServer(
    private val context: Context,
    private val serialProvider: () -> String
) {
    companion object {
        private const val TAG = "IHATTYS_GATT"

        // Stable UUIDs (keep constant)
        val SERVICE_UUID: UUID = UUID.fromString("0000A110-0000-1000-8000-00805F9B34FB")
        val SERIAL_CHAR_UUID: UUID = UUID.fromString("0000A111-0000-1000-8000-00805F9B34FB")
    }

    private var gattServer: BluetoothGattServer? = null

    private val callback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.i(TAG, "conn device=${device.address} status=$status newState=$newState")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid != SERIAL_CHAR_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                return
            }

            val bytes = serialProvider().toByteArray(Charsets.UTF_8)
            val slice =
                if (offset >= bytes.size) byteArrayOf()
                else bytes.copyOfRange(offset, bytes.size)

            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, slice)
        }
    }

    fun start(): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = manager.openGattServer(context, callback) ?: return false

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val serialChar = BluetoothGattCharacteristic(
            SERIAL_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(serialChar)
        gattServer?.addService(service)

        Log.i(TAG, "GATT server started")
        return true
    }

    fun stop() {
        try { gattServer?.close() } catch (_: Throwable) {}
        gattServer = null
        Log.i(TAG, "GATT server stopped")
    }
}
