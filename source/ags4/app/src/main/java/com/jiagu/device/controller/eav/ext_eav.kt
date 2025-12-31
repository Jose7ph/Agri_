package com.jiagu.device.controller.eav

import com.jiagu.device.controller.ControllerFactory


fun ControllerFactory.registerEavController() {
    registerController("EAV-RC50") { l, _ -> EavController(l) }

    registerVideo("EAV-RC50") { c, type ->
        val defUrl = "rtsp://192.168.144.108:554/stream=0"
        val url = rtspUrl.ifBlank { defUrl }
        val video = EavVideo(c, url)
        video.addIncompatibleCodec("c2.qti.hevc.decoder")
        video
    }
}
