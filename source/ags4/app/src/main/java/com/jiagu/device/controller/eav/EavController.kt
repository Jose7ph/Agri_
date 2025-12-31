package com.jiagu.device.controller.eav

import android.content.Context
import com.jiagu.api.communication.StreamChannel
import com.jiagu.api.communication.TcpClient
import com.jiagu.api.communication.UartChannel
import com.jiagu.device.channel.IChannel
import com.jiagu.device.controller.Controller
import com.jiagu.device.controller.EavProtocol
import com.jiagu.device.controller.EavTcpProtocol
import com.jiagu.device.controller.IController
import com.jiagu.jni.SecurityChecker

class EavController(listener: IController.Listener) : Controller(listener) {

    override fun connect(context: Context, device: String) {
        if (!SecurityChecker.test()) return
        connectDevice()
        eav.restData()
    }

    override fun destroy() {
        uartRc.close()
        uartFc.close()
    }

    private inner class RssiClient : TcpClient("192.168.144.100", 16000) {
        private var time: Long = 0
        override fun processReceived(data: ByteArray, len: Int) {
            eavRssi.onData(data)
        }
        override fun idle() {
            val t = System.currentTimeMillis()
            if (t - time > 1000) {
                time = t
                eavRssi.requireRssi()
            }
        }
    }

    private val rcListener = object : StreamChannel.StreamListener {
        private var current = System.currentTimeMillis()
        override fun onStreamConnected() {
//            eav.endChannelConfig()
//            eav.startChannelConfig()
        }
        override fun onStreamDisconnected() {

        }
        override fun onData(data: ByteArray, len: Int) {
            if (len > 0) {
               eav.onData(data)
            }
            val t = System.currentTimeMillis()
            if (t - current > 250) {
                current = t
                eav.handleTimeout()
            }
        }
    }

    private val fcListener = object : StreamChannel.StreamListener {
        override fun onStreamConnected() {listener.onControllerState("connection", CONNECTED)}
        override fun onStreamDisconnected() {listener.onControllerState("connection", DISCONNECTED)}
        override fun onData(data: ByteArray, len: Int) {
            val buf = ByteArray(len)
            System.arraycopy(data, 0, buf, 0, len)
            listener.onRadioData(0, buf)
        }
    }
    private val uartRc = UartChannel("/dev/ttyHS1", 115200)
    private val uartFc = UartChannel("/dev/ttyHS2", 115200)
    private val tcp = RssiClient()

    private val tcpWriter = object : IChannel.IWriter {
        override fun write(data: ByteArray) = tcp.send(data)
    }
    private val eavRssi = EavTcpProtocol(tcpWriter, listener)

    private val uartWriter = object : IChannel.IWriter {
        override fun write(data: ByteArray) = uartRc.send(data)
    }
    private val eav = EavProtocol(uartWriter, listener)
    private fun connectDevice() {
        uartRc.open(rcListener)
        uartFc.open(fcListener)
    }

    override fun readParameters() {
        eav.readParameters()
    }

    override fun setParameters(cmd: String, value: String) = eav.setParameters(cmd, value)
    override fun sendRadio(index: Int, data: ByteArray) = uartFc.send(data)
    override fun readId() = eav.readId()

    override fun sendRadioRtcm(rtcm: ByteArray) {}

    override fun pushButtonHandler(handler: IController.ButtonHandler) = eav.pushButtonHandler(handler)
    override fun popButtonHandler() = eav.popButtonHandler()
}
