package com.jiagu.device.controller

import android.view.View
import com.jiagu.api.video.IVideoListener

interface IVideo {

    companion object {
        const val CAP_LED = 1
        const val CAP_SWITCH = 2
    }

    fun asView(): View

    fun queryCapability(cap: Int): Boolean = false
    fun toggleLed() {}
    fun switchCamera(num: Int) {}
    fun setVideoListener(l: IVideoListener) {}
}
