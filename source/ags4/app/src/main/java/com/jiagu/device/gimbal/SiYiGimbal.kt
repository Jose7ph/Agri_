package com.jiagu.device.gimbal

import com.jiagu.api.communication.UdpClient
import com.jiagu.device.channel.IChannel

// A2-mini
class SiYiGimbal : IGimbal, IChannel.IWriter {
    override fun Capabilities(): IntArray {
        return intArrayOf()
    }

    override fun getSources(): Array<IGimbal.VideoSource> {
        return arrayOf(
            IGimbal.VideoSource("", "rtsp://192.168.144.25:8554/main.264", 0, 0, 0)
        )
    }

    override fun gimbalCtrl(cmd: Int) {
        when (cmd) {
            IGimbal.CTL_CENTER -> udp.send(siYiProtocol.setGimbalCmd(0x08, byteArrayOf(1)))
            IGimbal.CTL_DOWN -> {}
            IGimbal.CTL_STOP -> udp.send(siYiProtocol.setYawPitchRoll(0f, 0f, 0f))
        }
    }

    private val siYiProtocol = SiYiGimbalProtocol(this)
    private class GimbalClient(h: String, p: Int): UdpClient(h, p) {
        override fun processReceived(data: ByteArray, len: Int) {}
    }
    private val udp = GimbalClient("192.168.144.25", 37260)

    override fun gimbalMove(yaw: Float, pitch: Float, roll: Float) {
        val data = siYiProtocol.setYawPitchRoll(yaw, pitch, roll)
        udp.send(data)
    }

    override fun write(data: ByteArray) {}
}