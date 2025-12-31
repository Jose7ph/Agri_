package com.jiagu.device.controller

import java.lang.Integer.min
import java.util.concurrent.ConcurrentLinkedDeque

class ButtonHelper {
    private val handlers = ConcurrentLinkedDeque<IController.ButtonHandler>()
    private var lastValues: IntArray? = null
    fun processButton(channelMapping: Array<String>, channelValues: IntArray) {
        if (lastValues == null) {
            lastValues = channelValues
            return
        }
        val last = lastValues ?: return
        val count = min(channelValues.size, channelMapping.size + 4)
        for (idx in 4 until count) {
            val element = channelMapping[idx - 4]
            val key = if (element.isNotEmpty()) element.substring(1) else ""
            for (h in handlers) {
                if (h.onButton(key, last[idx], channelValues[idx])) {
                    break
                }
            }
        }
    }

    fun pushHandler(handler: IController.ButtonHandler) {
        handlers.addFirst(handler)
    }

    fun popHandler() {
        handlers.removeFirst()
    }
}