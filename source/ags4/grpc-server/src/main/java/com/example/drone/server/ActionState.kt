package com.example.drone.server

import java.util.concurrent.atomic.AtomicBoolean

class ActionState {
    private val holdEnabled = AtomicBoolean(false)

    fun setHold(enabled: Boolean) {
        holdEnabled.set(enabled)
    }

    fun isHoldEnabled(): Boolean {
        return holdEnabled.get()
    }
}
