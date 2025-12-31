package com.jiagu.ags4.bean

import com.jiagu.jgcompose.rtsp.RtspInfo

/**
 * type 0 摄像头 1 云台
 */
enum class RtspEnum(val key: String, val url: String, val type: Int) {
    SPRINT_LINK("SprintLink", "rtsp://192.168.199.18/", 0),
//    SPRINT_LINK("SprintLink", "rtsp://admin:admin@192.168.199.18", 0),
    SI_YI("SiYi", "rtsp://192.168.144.25:8554/main.264", 0),

    //SI_YI_2("SiYi-2", "rtsp://192.168.144.26:8554/main.264", 0),
    SKYDROID("Skydroid", "rtsp://192.168.144.108:554/stream=0", 0),
    H16_FPV("H16-FPV", "rtsp://192.168.0.10:8554/H264Video", 0),
    SIYI_A2_MINI("SiYi A2 Mini", "rtsp://192.168.144.25:8554/main.264", 1),
    SIYI_A8_MINI("SiYi A8 Mini", "rtsp://192.168.144.25:8554/main.264", 2),
    C10_SKYDROID("C10 skydroid", "rtsp://192.168.144.108:554/stream=0", 1),
    C10P_SKYDROID("C10P skydroid", "rtsp://192.168.144.108:554/stream=0", 2),
    AI_CM1("AI-CM1", "rtsp://192.168.144.88:8554/main", 1);

    companion object {
        fun toRtspInfo(): List<RtspInfo> {
            val rtspEnums = entries.toTypedArray()
            val rtspInfos = mutableListOf<RtspInfo>()
            rtspEnums.forEach {
                rtspInfos.add(
                    RtspInfo(
                        it.key,
                        it.url,
                        it.type
                    )
                )
            }
            return rtspInfos.toList()
        }
    }
}