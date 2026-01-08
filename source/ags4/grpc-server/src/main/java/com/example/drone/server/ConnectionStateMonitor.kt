package com.example.drone.server

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class ConnectionStateMonitor(initial: Boolean = true) {
    private val connected = AtomicBoolean(initial)
    private val listeners = CopyOnWriteArrayList<(Boolean) -> Unit>()

    fun isConnected(): Boolean = connected.get()

    fun setConnected(value: Boolean) {
        val old = connected.getAndSet(value)
        if (old != value) {
            Log.i("CORE", "ConnectionState changed | $old -> $value | listeners=${listeners.size}")
            listeners.forEach { it(value) }
        }
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
        Log.i("CORE", "Listener added | total=${listeners.size}")
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
        Log.i("CORE", "Listener removed | total=${listeners.size}")
    }
}
