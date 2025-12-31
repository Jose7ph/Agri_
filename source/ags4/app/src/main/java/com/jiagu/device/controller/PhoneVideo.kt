package com.jiagu.device.controller

import android.content.Context
import android.view.View

class PhoneVideo(context: Context) : View(context), IVideo {
    override fun asView(): View = this
}