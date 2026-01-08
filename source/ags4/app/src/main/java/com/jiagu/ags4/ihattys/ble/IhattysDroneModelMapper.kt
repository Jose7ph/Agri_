package com.jiagu.ags4.ihattys.ble

import com.jiagu.ags4.repo.net.model.DroneDevice

object IhattysDroneModelMapper {

    /**
     * IHATTYS Manufacturer Data MUST contain:
     * vendor_id + vendor_name + serial
     *
     * Source:
     * DroneModel.detail.value?.staticInfo  --> DroneDevice
     */
    fun readIdentityOrNull(device: DroneDevice?): IhattysIdentity? {
        if (device == null) return null

        // -------------------------
        // 1) SERIAL (required)
        // Prefer manufacturer/plate if present, else fallback to droneId
        // -------------------------
        val serialRaw = (device.zzDroneNum ?: "").trim().ifEmpty { device.droneId.trim() }
        if (serialRaw.isEmpty()) return null

        // SHGM PoC shows truncation to 9 characters.
        val serial9 = serialRaw.take(9)

        // -------------------------
        // 2) VENDOR NAME (required)
        // Best match in DroneDevice: zzAccountName
        // -------------------------
        val vendorName = device.zzAccountName.trim().ifEmpty { "Unknown" }

        // -------------------------
        // 3) VENDOR ID (required)
        // You said: use zzAccountId.
        //
        // The pasted DroneDevice does not contain zzAccountId.
        // So we safely try reflection:
        // - if field exists: use it
        // - else: fallback to stable derived ID
        // -------------------------
        val vendorIdLong =
            device.readLongFieldOrNull("zzAccountId")
                ?: device.readLongFieldOrNull("vendorId")
                ?: vendorName.stableVendorIdFallback()

        val vendorIdU16 = vendorIdLong.toUInt16()

        return IhattysIdentity(
            vendorId = vendorIdU16,
            vendorName = vendorName,
            serial = serial9
        )
    }

    /**
     * Convert any Long to an unsigned 16-bit int range: 0..65535
     */
    private fun Long.toUInt16(): Int {
        val mod = this % 65536L
        return if (mod < 0) (mod + 65536L).toInt() else mod.toInt()
    }

    /**
     * Reflection-safe read: does not break build if property doesn't exist.
     * Works well because DroneDevice is @Keep in your code.
     */
    private fun DroneDevice.readLongFieldOrNull(fieldName: String): Long? {
        return try {
            val f = this.javaClass.getDeclaredField(fieldName)
            f.isAccessible = true
            val v = f.get(this) ?: return null
            when (v) {
                is Long -> v
                is Int -> v.toLong()
                is String -> v.toLongOrNull()
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Fallback if zzAccountId doesn't exist yet.
     * Stable for the same vendorName across runs.
     */
    private fun String.stableVendorIdFallback(): Long {
        // positive 32-bit hash
        return (this.hashCode().toLong() and 0x7FFFFFFF)
    }
}
