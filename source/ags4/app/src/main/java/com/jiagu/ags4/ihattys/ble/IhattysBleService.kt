package com.jiagu.ags4.ihattys.ble

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class IhattysBleService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START_IHATTYS_BLE"
        const val ACTION_STOP = "ACTION_STOP_IHATTYS_BLE"

        private const val CHANNEL_ID = "ihatt ys_ble"
        private const val NOTIF_ID = 1101
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val identityProvider = IhattysIdentityProvider()
    private lateinit var gatt: IhattysGattServer
    private lateinit var adv: IhattysAdvertiser

    private var running = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        identityProvider.start()
        gatt = IhattysGattServer(this) { identityProvider.identity.value?.serial ?: "Unknown" }
        adv = IhattysAdvertiser(this, manufacturerId = IhattysVendorConfig.MANUFACTURER_ID)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopBleAndSelf()
            else -> startBleIfNeeded()
        }
        return START_STICKY
    }

    private fun startBleIfNeeded() {
        if (running) return
        running = true

        startForeground(NOTIF_ID, buildNotification("IHATTYS BLE broadcasting"))

        gatt.start()

        val serviceUuid = ParcelUuid(IhattysVendorConfig.SERVICE_UUID)

        serviceScope.launch {
            identityProvider.identity.collectLatest { id ->
                if (!running) return@collectLatest
                if (id == null) return@collectLatest

                val payload = IhattysManufacturerData.encode(id)
                adv.start(payload, serviceUuid)
            }
        }
    }

    private fun stopBleAndSelf() {
        if (!running) {
            stopSelf()
            return
        }
        running = false

        try { serviceScope.cancel() } catch (_: Throwable) {}
        try { adv.stop() } catch (_: Throwable) {}
        try { gatt.stop() } catch (_: Throwable) {}
        try { identityProvider.stop() } catch (_: Throwable) {}

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBleAndSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("IHATTYS")
            .setContentText(text)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "IHATTYS BLE", NotificationManager.IMPORTANCE_LOW)
        )
    }
}
