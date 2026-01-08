package com.jiagu.ags4.ihattys.ble

import java.nio.ByteBuffer
import java.nio.ByteOrder

object IhattysManufacturerData {

    /**
     * Format:
     * [0]    version=1
     * [1..2] vendorId (uint16 LE)
     * [3]    vendorNameLen
     * [...]  vendorName UTF-8 (max 12)
     * [..]   serialLen
     * [...]  serial UTF-8 (max 9)
     */
    fun encode(id: IhattysIdentity): ByteArray {
        val vendorNameBytes = id.vendorName.trim().toUtf8Max(12)
        val serialBytes = id.serial.trim().toUtf8Max(9)

        val total = 1 + 2 + 1 + vendorNameBytes.size + 1 + serialBytes.size
        val buf = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN)

        buf.put(1) // version
        buf.putShort(id.vendorId.coerceIn(0, 65535).toShort())

        buf.put(vendorNameBytes.size.toByte())
        buf.put(vendorNameBytes)

        buf.put(serialBytes.size.toByte())
        buf.put(serialBytes)

        return buf.array()
    }

    private fun String.toUtf8Max(max: Int): ByteArray {
        val b = toByteArray(Charsets.UTF_8)
        return if (b.size <= max) b else b.copyOfRange(0, max)
    }
}
