package com.jiagu.device.controller

import android.util.Log
import androidx.annotation.NonNull
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.device.channel.IChannel
import com.jiagu.device.controller.IController.Companion.TYPE_KEYSTR
import com.jiagu.device.controller.IController.Companion.TYPE_LEVERS
import com.jiagu.device.controller.IController.Companion.TYPE_ROLLER_PLUS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class EavProtocol(private val writer: IChannel.IWriter, private val listener: IController.Listener) : SiYiBaseProtocol(EAV_HEADER), IController {

    private var joytype: Byte = 0
    //    ch1
    //    ch2
    //    ch3
    //    ch4
    //    ch5 档位 模式
    //    ch6 预留1 L1
    //    ch7 预留2 L2
    //    ch8 预留3 L3
    //    ch9 返航 H
    //    ch10 预留4 R1
    //    ch11 喷洒 R2
    //    ch12 手自动 R3
    //    ch13 悬停 ||
    //    ch14 夜航灯 √
    //    ch15 左拨轮
    //    ch16 右拨轮
    private val channelMapping = Array(16) { "" }
    private val button = ButtonHelper()
    private var askingId = false // 1:ID
    private var askingType = false // 2:type
    private var askingMapping = false // 通道值
    private val askMap = ConcurrentHashMap<Byte, Boolean>(8)
    private var channelJob: Job? = null
    private var typeJob: Job? = null

    override fun sendRadio(index: Int, @NonNull data: ByteArray) {
        writer.write(data)
    }

    override fun readId() {
        askingId = true
        sendReadId()
    }

    private fun sendReadId() {
        val out = wrapCommand(0x16, null)
        writer.write(out)
    }

    override fun readParameters() {
        askingType = true
        askingMapping = true
        channelJob?.cancel()
        typeJob?.cancel()
        channelJob = GlobalScope.launch(Dispatchers.IO) {
            repeat(3){
                sendReadChannel()
                delay(300)
            }
        }
        typeJob = GlobalScope.launch(Dispatchers.IO) {
            repeat(3){
                sendReadType()
                delay(300)
            }
        }


    }




    private fun sendReadType() {
        val out = wrapCommand(0x0B, null)
        writer.write(out)
    }

    fun sendReadChannel() {
        val out = wrapCommand(0x1A, null)
        writer.write(out)
    }

    fun startChannelConfig() {
        val out = wrapCommand(0x1A, null)
        writer.write(out)
    }

    fun endChannelConfig() {
        val out = wrapCommand(0x1C, null)
        writer.write(out)
    }

    override fun setParameters(@NonNull cmd: String, @NonNull value: String) {
        when (cmd) {
            "type" -> setType(value)
            "version" -> requireVersion()
            "pair" -> pairing()
            "config" -> sendDefaultConfig()
        }
    }

    override fun sendRadioRtcm(@NonNull rtcm: ByteArray) {
    }

    override fun parseSiYi() {
        if (ownedLen <= 0) return
        when (owned[7].toInt()) {
            0x1 -> // pairing
                parsePairing()
//            0x0A -> // 通道数据上报
//                parseChannelReport()
            0x0B -> {
                parseChannels()
                val receiveCmdId: Byte = 0x1B
                if (askMap.containsKey(receiveCmdId)) {
                    askMap[receiveCmdId] = true
                }
            }
            0x0D -> parseType()
            0x0E -> // version
                parseVersion()
            0x19 -> parseId()
            0x00 -> //通用应答
                parseCommonRes()
            0x11 -> sendHeartbeat()
        }
    }

    private fun parseId() {
        askingId = false
        var id = String(owned, 8, 30)
        val available = owned[38].toInt()
        if (available <= 30) {
            id = id.substring(0, available)
        }
        listener.onControllerState("id", id)
    }

    private fun parseType() {
        joytype = owned[8]
        askingType = false
        when (joytype.toInt()) {
            2 -> listener.onControllerState("type", "jp")
            1 -> listener.onControllerState("type", "us")
            3 -> listener.onControllerState("type", "cn")
            else -> listener.onControllerState("type", "unknown")
        }
        typeJob?.cancel()
    }

    private fun parsePairing() {
        if (owned[8].toInt() != 1) return
        when (owned[9].toInt()) {
            0 -> listener.onControllerState("pairing", "ok")
            1 -> listener.onControllerState("pairing", "fail")
            2 -> listener.onControllerState("pairing", "pairing")
        }
    }

    private fun parseChannels() {
        askingMapping = false
        val r = MemoryHelper.MemoryReader(owned, 8, ownedLen - 10)
        val chs = IntArray(16)
        for (i in 0 until 16) {
            r.skip(6)
            chs[i] = r.readLEShort().toInt()
            Log.v("shero", "---------------------------------")
            Log.v("shero", "ch[$i]${chs[i]}")
            r.skip(1)
            val code = r.readUByte().toInt()
            Log.v("shero", "code:$code")
            if (i >= 4) {
                channelMapping[i - 4] = if (eavMap.containsKey(code)) eavMap[code]!! else ""
            }
        }
        Log.v("shero", "channelMapping:${Arrays.toString(channelMapping)}")
        listener.onControllerState("key", channelMapping)
        channelJob?.cancel()
    }

    // 添加新方法处理通道数据上报
    private fun parseChannelReport() {
        if (ownedLen < 38) return // 头部8字节 + 数据30字节

        val r = MemoryHelper.MemoryReader(owned, 8, 30)
        val chs = IntArray(16)

        // 读取15个通道数据
        // 272-1712
        for (i in 0 until 15) {
            val v = r.readLEShort()
            chs[i] = (v - 272) * 10 / 14 + 1000
        }

        // 处理通道数据
        button.processButton(channelMapping, chs)
    }

    private fun parseVersion() {
        val s = String.format(Locale.US, "%d.%d.%d", owned[8].toInt() and 0xFF, owned[9].toInt() and 0xFF, owned[10].toInt() and 0xFF)
        listener.onControllerState("version", IController.RcVersion(s, "", "", ""))
    }

    private fun setType(type: String) {
        val t: Byte = when (type) {
            "us" -> 1
            "jp" -> 2
            "cn" -> 3
            else -> 0
        }
        joytype = t
        val cmd = byteArrayOf(t)
        val out = wrapCommand(0x04, cmd)
        writer.write(out)
    }

    private fun requireVersion() {
        val out = wrapCommand(0x0C, null)
        writer.write(out)
    }

    private fun pairing() {
        val cmd = byteArrayOf(1, 2)
        val out = wrapCommand(0x03, cmd)
        writer.write(out)
    }

    override fun pushButtonHandler(@NonNull handler: IController.ButtonHandler) {
        button.pushHandler(handler)
    }

    override fun popButtonHandler() {
        button.popHandler()
    }

    fun handleTimeout() {
        if (askingId) sendReadId()
        if (askingType) sendReadType()
        if (askingMapping) sendReadChannel()
    }

    private fun sendHeartbeat() {
        val cmdId: Byte = 0x00
        val cmd = byteArrayOf(0x11, 0x00)
        sendCommand(cmdId.toInt(), cmd)
    }

    protected fun sendDefaultConfig() {
        val startCmdId: Byte = 0X1A
        val endCmdId: Byte = 0X1C
        //防止之前通道未关闭先关闭通道
        awaitSend(endCmdId, null, 200);
        //发送进入通道设置命令
        askMap[startCmdId] = false
        sendCommand(startCmdId.toInt(), null)
        val res = awaitAsk(startCmdId, 200)
        if (!res) {
            listener.onControllerState("config", "send 0X1A error....")
            Log.e("zhy", "send 0X1A error....")
            LogFileHelper.log("sendDefaultConfig -> send 0X1A error....")
            return
        }
        //通道设置
        sendConfig()
        listener.onControllerState("config", "ok")
    }

    private fun sendConfig() {
        val cmdId: Byte = 0x1B
        val c1 = byteArrayOf(0x02, 0x01, 0x02) //通道2设置 - 反向
        askMap[cmdId] = false
        sendCommand(cmdId.toInt(), c1)
        var res = awaitAsk(cmdId, 200)
        if (!res) {
            listener.onControllerState("config", "send command[$cmdId] error.")
            Log.e("zhy", "send command[$cmdId] error.")
            LogFileHelper.log("sendConfig -> send command[$cmdId] error.")
            //todo 未收到channel包
        }
        val c2 = byteArrayOf(0x05, 0x03, 0x08) //通道5设置 - 飞行挡位(模式)
        askMap[cmdId] = false
        sendCommand(cmdId.toInt(), c2)
        res = awaitAsk(cmdId, 200)
        if (!res) {
            listener.onControllerState("config", "send command[$cmdId] error.")
            Log.e("zhy", "send command[$cmdId] error.")
            LogFileHelper.log("sendConfig -> send command[$cmdId] error.")
            //todo 未收到channel包
        }
    }

    private fun awaitSend(cmdId: Byte, cmd: ByteArray?, awaitTime: Long) {
        sendCommand(cmdId.toInt(), cmd)
        awaitTime(awaitTime)
    }

    private fun awaitTime(time: Long) {
        val current = System.currentTimeMillis()
        while (true) {
            val t = System.currentTimeMillis()
            if (t - current > time) {
                break
            }
        }
    }

    private fun awaitAsk(cmdId: Byte, awaitTime: Long): Boolean {
        val retryMaxCount = 5
        var count = 0
        var current = System.currentTimeMillis()
        //等待回复
        while (true) {
            val t = System.currentTimeMillis()
            if (t - current > awaitTime) {
                current = t
                val result = askMap[cmdId]
                if (result != null && result) {
                    return true
                }
                count++
                if (count == retryMaxCount) {
                    return false
                }
            }
        }
    }

    private fun sendCommand(cmdId: Int, cmd: ByteArray?) {
        val out = wrapCommand(cmdId, cmd)
        writer.write(out)
    }

    private fun parseCommonRes() {
        val r = MemoryHelper.MemoryReader(owned, 8, 2)
        val cmdId = r.readByte()
        val code = r.readByte()
        Log.d("zhy", "通用应答[$cmdId]: $code")
        LogFileHelper.log("parseCommonRes -> [$cmdId]: $code")
        if (askMap.containsKey(cmdId)) {
            //信息重置只要收到就算成功
            if (cmdId == 0x17.toByte()) {
                askMap[cmdId] = true
                return
            }
            val success = code == 0.toByte()
            askMap[cmdId] = success
        }
    }

    fun restData() {
        val cmdId: Byte = 0x17
        askMap[cmdId] = false
        var retryMaxCount = 0
        while (true) {
            if (retryMaxCount >= 5) {
                Log.e("zhy", "retry count >= 5")
                break
            }
            sendCommand(cmdId.toInt(), null)
            val success = awaitAsk(cmdId, 100)
            if (success) {
                break
            }
            retryMaxCount++
        }
    }


    companion object {
        private val eavMap = HashMap<Int, String>()

        init {
            eavMap[5] = "${TYPE_KEYSTR}R2"
            eavMap[6] = "${TYPE_KEYSTR}√"
            eavMap[7] = "${TYPE_KEYSTR}R3"
            eavMap[8] = "${TYPE_LEVERS}MODE"
            eavMap[9] = "${TYPE_KEYSTR}PAUSE"
            eavMap[10] = "${TYPE_KEYSTR}H"
            eavMap[13] = "${TYPE_KEYSTR}L1"
            eavMap[14] = "${TYPE_KEYSTR}L2"
            eavMap[15] = "${TYPE_KEYSTR}L3"
            eavMap[16] = "${TYPE_KEYSTR}R1"
            eavMap[11] = "${TYPE_ROLLER_PLUS}LD"
            eavMap[12] = "${TYPE_ROLLER_PLUS}RD"
            eavMap[17] = "${TYPE_KEYSTR}A"
            eavMap[18] = "${TYPE_KEYSTR}B"
            eavMap[19] = "${TYPE_KEYSTR}BACK"
        }

        private val EAV_HEADER = byteArrayOf(0x66, 0x55)
    }
}