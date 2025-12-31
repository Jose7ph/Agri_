package com.jiagu.device.controller

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedDeque

abstract class UdpServer(private val rcvPort: Int, private val sendPort: Int, private val bufferSize: Int) : Thread() {

    private val queue = ConcurrentLinkedDeque<DatagramPacket>()
    private var stop = false
    private var receiveFromPort = 0
    private var wakeupPack: DatagramPacket? = null
    fun setReceiveFromPort(port: Int) {
        receiveFromPort = port
    }

    fun setWakeupPacket(addr: String, port: Int) {
        wakeupPack = DatagramPacket("hello".toByteArray(), 5, InetAddress.getByName(addr), port)
    }

    abstract fun processData(data: ByteArray)

    fun sendData(addr: String, port: Int, data: ByteArray) {
        val packet = DatagramPacket(data, data.size, InetAddress.getByName(addr), port)
        queue.add(packet)
    }

    fun sendData(data: ByteArray) = sendData("127.0.0.1", sendPort, data)

    fun stopServer() {
        stop = true
    }

    private fun send(udp: DatagramSocket) {
        var packet = queue.poll()
        while (packet != null) {
            udp.send(packet)
            packet = queue.poll()
        }
    }

    override fun run() {
        val udp: DatagramSocket
        try {
            udp = DatagramSocket(rcvPort)
        } catch (e: Exception) {
            return
        }
        udp.soTimeout = 50
        val buf = ByteArray(bufferSize)
        val packet = DatagramPacket(buf, bufferSize)
        Log.d("yuhang", "udp socket start on $rcvPort")
        var t0 = System.currentTimeMillis()
        while (!stop) {
            try {
                udp.receive(packet)
                if (receiveFromPort == 0 || (packet.port == receiveFromPort)) {
                    if (packet.length > 0) {
                        val data = ByteArray(packet.length)
                        System.arraycopy(buf, 0, data, 0, packet.length)
                        processData(data)
                    }
                }
            } catch (e: Exception) {
                wakeupPack?.let {
                    val t = System.currentTimeMillis()
                    if (t - t0 > 2000) {
                        Log.d("yuhang", "udp: send wakeup packet")
                        udp.send(it)
                        t0 = t
                    }
                }
            }
            send(udp)
        }
        udp.close()
    }
}