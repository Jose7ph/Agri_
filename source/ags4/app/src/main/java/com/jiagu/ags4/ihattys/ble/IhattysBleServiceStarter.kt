package com.jiagu.ags4.ihattys.ble

import android.content.Context
import android.content.Intent

object IhattysBleServiceStarter {

    fun start(context: Context) {
        val i = Intent(context, IhattysBleService::class.java).apply {
            action = IhattysBleService.ACTION_START
        }
        context.startService(i)
    }

    fun stop(context: Context) {
        val i = Intent(context, IhattysBleService::class.java).apply {
            action = IhattysBleService.ACTION_STOP
        }
        context.startService(i)
    }
}
