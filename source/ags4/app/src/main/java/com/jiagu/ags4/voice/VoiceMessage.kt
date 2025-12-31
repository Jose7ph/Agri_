package com.jiagu.ags4.voice

import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import org.greenrobot.eventbus.EventBus

class VoiceMessage(val msg: Any) {
    companion object {
        fun emit(voice: Any) {
            when (voice) {
                is VKAg.IMUData, is VKAg.WARNData, is String, is NewWarnTool.NewWarnStringData -> {
                    EventBus.getDefault().post(VoiceMessage(voice))
                }
            }
        }
    }
}