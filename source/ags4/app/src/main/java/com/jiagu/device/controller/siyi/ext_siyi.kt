package com.jiagu.device.controller.siyi

import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.controller.eav.EavVideo

fun ControllerFactory.registerSiYiController() {
    registerController(arrayOf("MK15", "UNIRC7")) { l, m -> MK15Controller(m, l) }

    registerVideo(arrayOf("MK15", "UNIRC7")) { c, type ->
        val url = "rtsp://192.168.144.25:8554/main.264"
        rtspUrl.ifBlank { url }
        EavVideo(c, rtspUrl).apply {
            if (type == "MK15") setUseTCP(true)
        }
    }
}
