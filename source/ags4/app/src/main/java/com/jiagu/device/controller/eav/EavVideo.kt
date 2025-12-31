package com.jiagu.device.controller.eav

import android.content.Context
import android.view.View
import com.jiagu.api.video.IVideoListener
import com.jiagu.api.widget.RtspVideoView
import com.jiagu.device.controller.IVideo

class EavVideo(ctx: Context, rtspurl: String) : RtspVideoView(ctx), IVideo {
    init {
        setUrl(rtspurl)
        setUseTCP(true)
    }

    override fun asView(): View = this
    override fun setVideoListener(l: IVideoListener) = setListener(l)

}