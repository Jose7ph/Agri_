package com.jiagu.device.controller.siyi

import android.content.Context
import com.jiagu.api.communication.StreamChannel
import com.jiagu.api.communication.UartChannel
import com.jiagu.device.channel.IChannel
import com.jiagu.device.controller.Controller
import com.jiagu.device.controller.IController
import com.jiagu.device.controller.SiYiProtocol

class MK15Controller(model: String, listener: IController.Listener)
    : Controller(listener), IChannel.IWriter {

    override fun connect(context: Context, device: String) {
        connectDevice()
    }

    override fun destroy() {
        serialPort.close()
    }

    override fun write(data: ByteArray) {
        serialPort.send(data)
    }

    private val rfListener = object : StreamChannel.StreamListener {
        private var current = System.currentTimeMillis()
        override fun onStreamConnected() {
            listener.onControllerState("connection", CONNECTED)
            siyi.setParameters("mapping", "")
            siyi.setParameters("channel", "")
        }
        override fun onStreamDisconnected() { listener.onControllerState("connection", DISCONNECTED) }
        override fun onData(data: ByteArray, len: Int) {
            val time = System.currentTimeMillis()
            if (time - current > 2000) {
                siyi.setParameters("rssi", "")
                current = time
            }
            if (len > 0) {
                val newData = ByteArray(len)
                System.arraycopy(data, 0, newData, 0, len)
                siyi.onData(newData)
            }
        }
    }
    private val serialPort = UartChannel(if (model == "UNIRC7") "/dev/ttyHS3" else "/dev/ttyHS0", 115200)
    private val siyi = SiYiProtocol(this, listener).apply { setProduct(model) }
    private fun connectDevice() {
        serialPort.open(rfListener)
    }

    override fun readParameters() {
        siyi.readParameters()
        siyi.setParameters("version", "")
    }

    override fun setParameters(cmd: String, value: String) = siyi.setParameters(cmd, value)
    override fun sendRadio(index: Int, data: ByteArray) = serialPort.send(data)
    override fun readId() = siyi.readId()

    override fun sendRadioRtcm(rtcm: ByteArray) {}

    override fun pushButtonHandler(handler: IController.ButtonHandler) = siyi.pushButtonHandler(handler)
    override fun popButtonHandler() = siyi.popButtonHandler()
}
