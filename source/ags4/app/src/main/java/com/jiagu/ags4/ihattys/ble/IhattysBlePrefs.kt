package com.jiagu.ags4.ihattys.ble

import android.content.Context

object IhattysBlePrefs {
    private const val SP_NAME = "ihatt ys_ble_sp"
    private const val KEY_RUNNING = "running"

    fun isRunning(context: Context): Boolean {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_RUNNING, false)
    }

    fun setRunning(context: Context, running: Boolean) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_RUNNING, running)
            .apply()
    }
}
