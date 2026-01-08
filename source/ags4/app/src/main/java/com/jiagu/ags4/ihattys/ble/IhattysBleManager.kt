package com.jiagu.ags4.ihattys.ble

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object IhattysBleManager {

    fun isRunning(context: Context): Boolean {
        return IhattysBlePrefs.isRunning(context)
    }

    /**
     * Toggle BLE:
     * - If running -> stop
     * - If stopped -> start
     * Returns new state: true = running, false = stopped
     */
    fun toggle(context: Context): Boolean {
        return if (isRunning(context)) {
            stop(context)
            false
        } else {
            start(context)
            true
        }
    }

    fun start(context: Context) {
        IhattysBlePrefs.setRunning(context, true)
        val i = Intent(context, IhattysBleService::class.java).apply {
            action = IhattysBleService.ACTION_START
        }
        ContextCompat.startForegroundService(context, i)
    }

    fun stop(context: Context) {
        IhattysBlePrefs.setRunning(context, false)
        val i = Intent(context, IhattysBleService::class.java).apply {
            action = IhattysBleService.ACTION_STOP
        }
        context.startService(i)
    }
}
