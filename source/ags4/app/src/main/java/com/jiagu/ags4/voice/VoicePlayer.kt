package com.jiagu.ags4.voice

import android.app.Service
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.jiagu.api.helper.DigestHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.jni.SpeexDecoder
import com.jiagu.tools.vm.VoiceTask
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sin

class VoicePlayer(val context: Context) {
    private var enabled = true
    private val audioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
    private val audioFocusHelper = AudioFocusHelper()
    private var audioTrack: AudioTrack? = null
    private var warnBuffer = ConcurrentLinkedQueue<String>()
    private var notifyBuffer = ConcurrentLinkedQueue<String>()
    init {
        initAudioTrack()
        if (audioFocusHelper.requestFocus()) {
            enableVoice(true)
        }
    }

    private fun enableVoice(enable: Boolean) {
        enabled = enable
        if (!enable) {
            warnBuffer.clear()
            notifyBuffer.clear()
        }
    }

    fun playNotify(text: String) {
        addIntoQueue(text, "")
    }

    fun playWarn(text: String) {
        addIntoQueue(text, "warn")
    }

    private fun addIntoQueue(text: String, type: String) {
        if (!enabled || startWarning) return
        val lang = VoiceTask.checkVoiceLanguage()
        val md5 = DigestHelper.md5sum(text)
        val dir = context.getExternalFilesDir("pcm")
        val spx = File(File(dir, lang), "$md5.spx")
        if (spx.exists()) {
            if (type == "warn") {
                if (!warnBuffer.contains(md5)) warnBuffer.add(md5)
            } else notifyBuffer.add(md5)
        }
    }

    fun destroy() {
        stop = true
        audioFocusHelper.abandonFocus()
        try {
            voiceThread?.join()
        } catch (e: InterruptedException) {
            Log.e("yuhang", "error: $e")
        }
    }

    fun isFree(): Boolean {
        return !startWarning && warnBuffer.isEmpty() && notifyBuffer.isEmpty()
    }

    private inner class VoiceThread : Thread() {
        override fun run() {
            val didiBuffer = ByteArray(16 * 2 * 300)
            initDidiBuffer(didiBuffer)
            val minBufferSize =
                AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val format = AudioFormat.Builder()
                .setSampleRate(16000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val track = AudioTrack(attr, format, minBufferSize, AudioTrack.MODE_STREAM, audioManager.generateAudioSessionId())
            audioTrack = track
            LogFileHelper.log("voice thread started")
            track.play()
            var currentBuffer: ByteArray? = null
            var isBreakable = false
            var startPosition = 0
            while (!stop) {
                try {
                    if (startWarning) {
                        if (currentBuffer == null) {
                            currentBuffer = generateWarning(didiBuffer)
                            startPosition = 0
                            warnBuffer.clear()
                            notifyBuffer.clear()
                        }
                    } else {
                        if (currentBuffer != null) {
                            if (isBreakable && warnBuffer.isNotEmpty()) {
                                currentBuffer = readSpeexFile(warnBuffer.poll()!!)
                                startPosition = 0
                                isBreakable = false
                                notifyBuffer.clear()
                            }
                        } else if (warnBuffer.isNotEmpty()) {
                            currentBuffer = readSpeexFile(warnBuffer.poll()!!)
                            startPosition = 0
                            isBreakable = false
                            notifyBuffer.clear()
                        } else if (notifyBuffer.isNotEmpty()) {
                            currentBuffer = readSpeexFile(notifyBuffer.poll()!!)
                            startPosition = 0
                            isBreakable = true
                        }
                    }
                    if (currentBuffer != null) {
                        val actual =
                            if (startPosition + minBufferSize > currentBuffer.size) currentBuffer.size - startPosition else minBufferSize
                        track.write(currentBuffer, startPosition, actual)
                        startPosition += actual
                        if (startPosition >= currentBuffer.size) {
                            currentBuffer = null
                        }
                    } else {
                        sleep(10)
                    }
                } catch (e: Exception) {
                    LogFileHelper.log("voice thread: $e")
                }
            }
            track.stop()
            track.release()
            audioTrack = null
            Log.d("yuhang", "voice thread stop")
        }
    }

    private var voiceThread: Thread? = null
    private var stop = false
    private fun initAudioTrack() {
        stop = false
        voiceThread = VoiceThread()
        voiceThread?.start()
    }

    private fun readSpeexFile(text: String): ByteArray? {
        val lang = VoiceTask.checkVoiceLanguage()
        val dir = context.getExternalFilesDir("pcm")
        val spx = File(File(dir, lang), "$text.spx")
        if (spx.exists()) {
            return SpeexDecoder.decodeFile(spx.absolutePath)
        }
        return null
    }

    private var warningInterval = 0
    private var didi = true
    private var startWarning = false
    private fun generateWarning(didiBuffer: ByteArray): ByteArray {
        val out: ByteArray
        if (didi) { // tone 1K + 2K
            out = didiBuffer
            if (warningInterval > 0) didi = false
        } else { // silence
            out = if (warningInterval == 0) didiBuffer else ByteArray(16 * warningInterval * 2)
            didi = true
        }
        return out
    }

    private fun initDidiBuffer(didiBuffer: ByteArray) {
        val data = ShortArray(16)
        for (i in 0 until 16) {
            data[i] = ((sin(i * 8 / Math.PI) + 0.5 * sin(i * 4 / Math.PI))* 27000).toInt().toShort()
        }
        for (i in data.indices) {
            MemoryHelper.LittleEndian.putShort(didiBuffer, i * 2, data[i])
        }
        for (i in 1 until 300) {
            System.arraycopy(didiBuffer, 0, didiBuffer, i * 32, 32)
        }
    }

    fun startWarning(interval: Int) {
        if (!startWarning) {
            startWarning = true
            didi = true
        }
        warningInterval = interval
    }

    fun stopWarning() {
        startWarning = false
        didi = false
    }

    // 音频焦点改变监听
    private inner class AudioFocusHelper : AudioManager.OnAudioFocusChangeListener {
        private var startRequested = false
        private var pausedForLoss = false
        private var currentFocus = 0
        override fun onAudioFocusChange(focusChange: Int) {
            if (currentFocus == focusChange) {
                return
            }
            currentFocus = focusChange
            Log.d("yuhang", "audio focus change: $focusChange")
            LogFileHelper.log("audio focus change: $focusChange")
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                    if (startRequested || pausedForLoss) {
                        enableVoice(true)
                        startRequested = false
                        pausedForLoss = false
                    } else {
                        audioTrack?.setVolume(1f)
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    pausedForLoss = true
                    enableVoice(false)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    audioTrack?.setVolume(0.1f)
                }
            }
        }

        fun requestFocus(): Boolean {
            if (currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
                return true
            }
            val status = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
                currentFocus = AudioManager.AUDIOFOCUS_GAIN
                return true
            }
            startRequested = true
            return false
        }

        fun abandonFocus(): Boolean {
            startRequested = false
            val status = audioManager.abandonAudioFocus(this)
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status
        }
    }
}
