package com.jiagu.device.controller

import android.content.Context

abstract class Controller(val listener: IController.Listener) : IController {

    companion object {
        const val DISCONNECTED = 0
        const val CONNECTING = 1
        const val CONNECTED = 2
    }

    abstract fun connect(context: Context, device: String)
    abstract fun destroy()
}
