package com.jiagu.ags4.ihattys.ble

import java.util.UUID

object IhattysVendorConfig {
    const val MANUFACTURER_ID = 2218

    val SERVICE_UUID: UUID = UUID.fromString("0000A110-0000-1000-8000-00805F9B34FB")
    val SERIAL_CHAR_UUID: UUID = UUID.fromString("0000A111-0000-1000-8000-00805F9B34FB")
}
