package com.jiagu.device.controller

interface IController {

    class RcVersion(val rc: String, val receiver: String, val gi: String, val si: String)

    interface Listener {
        // "connection" -> CONNECTED / CONNECTING / DISCONNECTED
        // "type" -> "jp" / "us" / "cn" / "unknown"
        // "id" -> ID
        // "pdt" -> BoardID
        // "location" -> RtkLatLng
        // "state" -> "ok" / "error"
        // "rssi" -> rssi
        // "key" -> Array<String>
        // "version" -> RcVersion
        fun onControllerState(name: String, value: Any)
        fun onRadioData(index: Int, data: ByteArray)
    }

    interface ButtonHandler {
        // key: key name
        // return: true - processed
        fun onButton(key: String, lastValue: Int, value: Int): Boolean
    }

    fun sendRadio(index: Int, data: ByteArray)

    fun readId()
    fun readParameters()
    fun setParameters(cmd: String, value: String)
    fun sendRadioRtcm(rtcm: ByteArray)

    fun pushButtonHandler(handler: ButtonHandler) {}
    fun popButtonHandler() {}

    companion object {
        const val TYPE_NONE = 0//不知道按键是什么时
        const val TYPE_ROCKER = 1//摇杆
        const val TYPE_KEYSTR = 2//按键
        const val TYPE_LEVERS = 3//拨杆
        const val TYPE_ROLLER_SIZE = 4//不回中 滚轮
        const val TYPE_ROLLER_PLUS = 5//回中 滚轮
        const val TYPE_NA = 9//按键设置----时
    }
}
