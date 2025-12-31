package com.jiagu.device.controller

import android.content.Context
import android.util.Log
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.device.channel.IChannel
import com.jiagu.device.channel.SppChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PhoneController(listener: IController.Listener) :
    Controller(listener), IChannel.IWriter, IChannel.IDataFilter, IChannel.ChannelListener {
    private var btaddr = ""
    private var brand = ""
    private var channel: IChannel? = null

    override fun connect(context: Context, device: String) {
        channel?.close()
        connectDevice(device)

//        connectDevice("00:0C:BF:09:71:31")//njjg001
//        connectDevice("00:0C:BF:09:3E:2D")//njjg_002
//        connectDevice("00:0C:BF:06:71:98")//T12_416
//        connectDevice("00:0C:BF:0F:2A:67")//Beilian
//        connectDevice("00:0C:BF:02:71:68")//FXQ002
//        connectDevice("00:0C:BF:15:5C:67")
//        connectDevice("00:0C:BF:04:7A:5A")
    }

    override fun onChannelConnected(connected: Boolean) {
        if (connected) {
            startProbe()
        }
        listener.onControllerState("connection", if (connected) CONNECTED else DISCONNECTED)
    }

    override fun write(data: ByteArray) {
        channel?.write(data)
    }

    private fun connectDevice(device: String) {
        btaddr = device
        listener.onControllerState("connection", CONNECTING)
        channel = SppChannel(device, this)
        channel?.open()
        channel?.addDataFilter(this)
    }

    private val fakeListener = object : IController.Listener {
        override fun onRadioData(index: Int, data: ByteArray) {
            listener.onRadioData(index, data)
        }
        override fun onControllerState(name: String, value: Any) {}
    }
    private val siyiListener = object : IController.Listener {
        override fun onRadioData(index: Int, data: ByteArray) {
            listener.onRadioData(index, data)
        }

        override fun onControllerState(name: String, value: Any) {
            if (skydroid != null) {
                found("SiYi")
                brand = "siyi"
                LogFileHelper.log("found siyi controller")
                controller = siyi!!
                finish(siyi)
            }
            listener.onControllerState(name, value)
        }
    }
    private val skydroidListener = object : IController.Listener {
        override fun onRadioData(index: Int, data: ByteArray) {
            listener.onRadioData(index, data)
        }

        override fun onControllerState(name: String, value: Any) {
            if (siyi != null) {
                found("skydroid")
                brand = "skydroid"
                LogFileHelper.log("found skydroid controller")
                controller = skydroid!!
                skydroid?.getRssi()
                listener.onControllerState("key", Helper.KEY_MAPPING)
                finish(skydroid)
            }
            listener.onControllerState(name, value)
        }
    }

    private var skydroid: SkydroidProtocol? = null
    private var siyi: SiYiProtocol? = null
    private var fake: FakeController? = FakeController(this, fakeListener)
    private var job: Job? = null
    private var controller: IController? = null

    override fun destroy() {
        channel?.close()
        finish(null)
    }

    override fun onData(data: ByteArray): ByteArray? {
        skydroid?.apply { onData(data) }
        siyi?.apply { onData(data) }
        fake?.apply { onData(data) }
        return null
    }

    private fun found(name: String) {
        Log.d("yuhang", "found $name controller")
        job?.cancel()
    }

    private fun finish(keep: IChannel.IDataFilter?) {
        job?.cancel()
        if (skydroid != keep) skydroid = null
        if (siyi != keep) siyi = null
        if (fake != keep) fake = null
    }

    override fun readId() {
        controller?.readId()
    }

    private fun startProbe() {
        job?.cancel()
        skydroid = SkydroidProtocol(this, skydroidListener)
        siyi = SiYiProtocol(this, siyiListener)
        controller = fake
        job = GlobalScope.launch {
            Log.d("yuhang", "looking for Skydroid/SiYi controller")
            skydroid?.readParameters()
            siyi?.readParameters()
            for (i in 0..2) {
                skydroid?.readParameters()
                siyi?.readParameters()
                delay(1500)
            }
            Log.d("yuhang", "found nothing")
            skydroid = null
            siyi = null
        }
    }

    override fun readParameters() {
        controller?.readParameters()
    }

    override fun setParameters(cmd: String, value: String) {
        controller?.setParameters(cmd, value)
    }

    fun changeDrone(id: Int) {
        skydroid?.changeDrone(id)
    }

    override fun sendRadio(index: Int, data: ByteArray) {
        controller?.sendRadio(index, data)
    }

    override fun sendRadioRtcm(rtcm: ByteArray) {
        controller?.sendRadioRtcm(rtcm)
    }
}