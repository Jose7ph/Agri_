package com.jiagu.device.controller

import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentLinkedDeque

abstract class SocketServer(private val bufferSize: Int) : Thread() {

    private val queue = ConcurrentLinkedDeque<ByteArray>()
    private var stop = false
    protected lateinit var input: InputStream
    protected lateinit var output: OutputStream

    abstract fun initInputOutput()
    abstract fun close()
    abstract fun processData(data: ByteArray)

    fun sendData(data: ByteArray) {
        queue.add(data)
    }

    fun stopServer() {
        stop = true
    }

    private fun send() {
        var data = queue.poll()
        while (data != null) {
            output.write(data)
            data = queue.poll()
        }
    }

    override fun run() {
        try {
            initInputOutput()
        } catch (e: Exception) {
            return
        }
        val buf = ByteArray(bufferSize)
        Log.d("yuhang", "socket start running")
        while (!stop) {
            try {
                val count = input.read(buf)
                if (count > 0) {
                    val data = ByteArray(count)
                    System.arraycopy(buf, 0, data, 0, count)
                    processData(data)
                }
            } catch (e: Exception) {
//                ​break
            }
            send()
        }
        close()
    }
}