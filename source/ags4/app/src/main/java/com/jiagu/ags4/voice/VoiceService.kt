package com.jiagu.ags4.voice

import android.content.Context
import android.util.Log
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.runTask
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.tools.ext.UnitHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class VoiceService(context: Context) {

    val app = context.applicationContext

    companion object {
        private var voiceService: VoiceService? = null
        fun start(context: Context) {
            voiceService = VoiceService(context)
        }

        fun stop() {
            voiceService?.destroy()
            voiceService = null
        }
    }

    private lateinit var player: VoicePlayer

    private fun checkRadar(data: VKAg.IMUData): Boolean {
        val fs = data.foundFObstacle()
        val bs = data.foundBObstacle()
        return if (fs > 0 || bs > 0) {
            val interval = if (fs == 2 || bs == 2) 0
            else {
                val fd = if (fs > 0) data.BiZhang1JuLi else 1000f
                val bd = if (bs > 0) data.BiZhang2JuLi else 1000f
                val dist = if (fd > bd) bd else fd
                when {
                    dist >= 10 -> 500
                    else -> (dist * 30 + 200).toInt()
                }
            }
            player.startWarning(interval)
            true
        } else {
            player.stopWarning()
            false
        }
    }

    private var flyMode = 0
    private fun mode2Voice(data: VKAg.IMUData) {
        if (data.flyMode.toInt() != flyMode) {
            flyMode = data.flyMode.toInt()
            val str = VKAgTool.modeToString(app, flyMode)
            if (str.isNotBlank()) {
                player.playWarn(str)
            }
        }
    }

    private var flyStatus = 0
    private fun status2Voice(data: VKAg.IMUData) {
        if (data.execFlag.toInt() != flyStatus) {
            flyStatus = data.execFlag.toInt()
            val str = VKAgTool.statusToString(app, flyStatus)
            if (str.isNotBlank()) {
                player.playWarn(str)
            }
        }
    }

    private var abMode = 0
    private fun ab2Voice(data: VKAg.IMUData) {
        if (abMode != data.ABStatus.toInt()) {
            abMode = data.ABStatus.toInt()
            val str = VKAgTool.abToString(app, abMode).second
            if (str.isNotBlank()) {
                player.playWarn(str)
            }
        }
    }

    private var warnTick = 0L
//    private var pwmData: VKAg.PWMData? = null
    private fun warning2Voice(data: VKAg.IMUData) {
        val time = System.currentTimeMillis()
        if (time - warnTick < 3000) return
        warnTick = time
        val str = VKAgTool.returnHoverToString(app, data, isSeedWorkType()).second
        if (str.isNotBlank()) {
            player.playWarn(str)
            when (data.flyMode) {
                VKAgCmd.FLYSTATUS_GCSXUANTING -> player.playWarn(app.getString(com.jiagu.v9sdk.R.string.voice_hovering))
                VKAgCmd.FLYSTATUS_GCSFANHANG,
                VKAgCmd.FLYSTATUS_YAOKONGQIFANHANG -> player.playWarn(app.getString(com.jiagu.v9sdk.R.string.voice_going_home))
            }
        }
    }

    private var voiceTick = 0L
    private fun warning2Voice(data: NewWarnTool.NewWarnStringData) {
//        val time = System.currentTimeMillis()
//        if (time - voiceTick < 2000) return
//        voiceTick = time
        if (data.voices.size > 5) {
            player.playWarn(app.getString(com.jiagu.v9sdk.R.string.voice_fcu_count_5))
        } else {
            for (w in data.voices) {
                when (w.warnType) {
                    NewWarnTool.WARN_TYPE_ERROR, NewWarnTool.WARN_TYPE_WARN -> player.playWarn(w.warnString)
                    NewWarnTool.WARN_TYPE_INFO -> player.playNotify(w.warnString)
                }
            }
        }
    }

    private var monitorTick = 0L
    private fun monitor2Voice(data: VKAg.IMUData) {
        val time = System.currentTimeMillis()
        if (time - monitorTick >= 10000 && player.isFree()) {
            monitorTick = time
            val voiceList = VKAgTool.monitor2String(app, data, UnitHelper.convertLength(1f))
            voiceList.forEach {
                player.playNotify(it)
            }
        }
    }

    private var isRadarWarn = false
    @Subscribe
    fun onVoiceMessage(voice: VoiceMessage) {
        when (val msg = voice.msg) {
//            is VKAg.WARNData -> {
//                val str = VKAgTool.warnListToString(app, msg).second
//                if (str.isNotBlank()) {
//                    player.playWarn(str)
//                }
//            }
//            is VKAg.PWMData -> pwmData = msg
            is NewWarnTool.NewWarnStringData -> {
                if (!isRadarWarn) {
                    warning2Voice(msg)
                }
            }
            is VKAg.IMUData -> {
                isRadarWarn = checkRadar(msg)
                if (!isRadarWarn) {
//                    warning2Voice(msg)
//                    status2Voice(msg)
//                    mode2Voice(msg)
//                    ab2Voice(msg)
                    monitor2Voice(msg)
                }
            }
            is String -> player.playWarn(msg)
        }
    }

    init {
        Log.d("yuhang", "voice service created")
        LogFileHelper.log("voice service created")
        runTask {
            player = VoicePlayer(app)
            EventBus.getDefault().register(this@VoiceService)
        }
    }

    fun destroy() {
        Log.d("yuhang", "voice service destroyed")
        LogFileHelper.log("voice service destroyed")
        EventBus.getDefault().unregister(this)
        player.destroy()
    }
}
