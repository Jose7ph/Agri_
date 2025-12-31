package com.jiagu.device.locator

import com.jiagu.device.channel.IChannel
import com.jiagu.device.channel.SppChannel
import com.jiagu.device.controller.NMEAFilter
import com.jiagu.device.locator.ILocator.LocatorListener
import com.jiagu.device.model.RtkLatLng

class Locator(btaddr: String, private val listener: LocatorListener)
    : ILocator, IChannel.ChannelListener, NMEAFilter.NMEAListener {

    private val channel = SppChannel(btaddr, this)
    private val filter: NMEAFilter = NMEAFilter(this)
    init {
        channel.addDataFilter(filter)
        channel.open()
    }

    override fun disconnect() {
        channel.close()
    }

    override fun startLocating() {}
    override fun stopLocating() {}

    override fun sendRTCM(rtcm: ByteArray) {
        channel.write(rtcm)
    }

    override fun onChannelConnected(connected: Boolean) {
        listener.onConnect(connected)
        if (!connected) channel.close()
    }

    override fun onGGA(location: RtkLatLng?) {
        location?.let {
            listener.onLocation(location)
        }
    }

    override fun onPdtInfo(id: String?) {
        listener.onLocatorId(id ?: "NA")
    }
}