package com.jiagu.ags4.bean

import java.util.Locale

class RtcmInfo(
    val type: Int, val rtkid: Long, var devtype: String,
    val qxak: String, val qxas: String, val qxid: String, val qxdsk: String, val qxdss: String?,
    val host: String, val mount: String, val user: String, val pass: String,
    val expired: Long, var active: Boolean, val remain: Int, var error: String? = null
) {
    companion object {
        const val CODE_DEFAULT = -8
        const val CODE_MANUAL_CLOSE_RTK = -1
        const val CODE_RTK_DATA_NORMAL = 2
        const val CODE_RTK_DATA_ABNORMAL = 3
    }

    var source: String = ""//QX/Ntrip/USB
    //-8默认code 不需要提示用户连接失败
    // 0-代表打开了线程
    // 大于0(RTCM的size)真实收到了RTCM数据
    // -1000 手动关闭/或者没打开线程 千寻：-101网络连接错误 -201超出服务范围 -301 -999服务异常
    // https://developer.qxwz.com/help/source/193508858
    var status: Int = CODE_DEFAULT

    override fun toString(): String {
        return String.format(Locale.US, "RTCMINFO [%s] status:%d, type:%d, rtkid:%d, devtype:%s, " +
                "qxak:%s, qxas:%s, qxid:%s, qxdsk:%s, qxdss:%s, " +
                "host:%s, mount:%s, user:%s, pass:%s, expired:%d, active:%s, remain:%d, error:%s",
            source, status, type, rtkid, devtype,
            qxak, qxas, qxid, qxdsk, qxdss,
            host, mount, user, pass, expired, active, remain, error)
    }
}