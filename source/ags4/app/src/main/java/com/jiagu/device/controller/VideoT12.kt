package com.jiagu.device.controller

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.View
import com.jiagu.api.communication.StreamChannel
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.widget.VideoView
import com.jiagu.device.channel.IChannel
import com.jiagu.device.channel.UsbChannel

class VideoT12(ctx: Context) : VideoView(ctx), IVideo, IChannel.ChannelListener,
    IChannel.IDataFilter, StreamChannel.IdleListener {

    private val usb = UsbChannel(ctx, this, 60000, 4292)
    init {
        usb.addDataFilter(this)
        usb.setIdleListener(this)
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, w: Int, h: Int) {
        super.onSurfaceTextureAvailable(surfaceTexture, w, h)
        usb.open()
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        usb.close()
        return super.onSurfaceTextureDestroyed(surfaceTexture)
    }

    override fun asView(): View = this

    override fun queryCapability(cap: Int): Boolean {
        return when (cap) {
            IVideo.CAP_LED, IVideo.CAP_SWITCH -> true
            else -> false
        }
    }

    private var led = false
    override fun toggleLed() {
        led = !led
        val cmd = if (led) "AT+LED -e1\r\n" else "AT+LED -e0\r\n"
        usb.write(wrapCmd(cmd))
    }

    override fun switchCamera(num: Int) {
        val cmd = when (num) {
            1 -> "AT+SWITCH -d1\r\n";
            else -> "AT+SWITCH -d0\r\n";
        }
        usb.write(wrapCmd(cmd))
    }

    override fun onChannelConnected(connected: Boolean) {}

    override fun onData(data: ByteArray): ByteArray? {
        if (data.size > 4 && data[3] == 0xA5.toByte()) {
            var size = MemoryHelper.BigEndian.toShort(data, 4).toInt() and 0xFFFF
            if (size > data.size - 4) size = data.size - 4
            if (size > 508) size = 508
            sendNALUStream(data, 4, size)
        }
        return null
    }

    private var t0 = 0L
    override fun onIdle() {
        val t = System.currentTimeMillis()
        if (t - t0 > 20) {
            t0 = t
            val out = byteArrayOf(0xFF.toByte(), 2, 0, 0x55, 0, 0xA5.toByte())
            usb.write(out)
            Log.d("yuhang", "idle: send")
        }
    }

    private fun wrapCmd(cmd: String): ByteArray {
        val out = ByteArray(cmd.length + 5)
        out[0] = 0xFF.toByte()
        out[1] = 2
        out[2] = 0
        out[3] = 0x55
        out[4] = (cmd.length - 1).toByte()
        System.arraycopy(cmd.toByteArray(), 0, out, 4, cmd.length)
        return out
    }
}
