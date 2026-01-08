package com.example.drone.server

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class ArmAuthorizationMonitor {

    // 0 means "no pending request"
    private val pendingSystemId = AtomicInteger(0)
    private val pendingSinceMs = AtomicLong(0)

    // ===== Your current DroneServer.kt expects these names =====
    fun getPendingSystemId(): Int = pendingSystemId.get()

    fun publishAuthorizationRequest(systemId: Int) {
        pendingSystemId.set(systemId)
        pendingSinceMs.set(System.currentTimeMillis())
    }

    fun clearPending() {
        pendingSystemId.set(0)
        pendingSinceMs.set(0)
    }

    // =====  helpers  =====
    fun pendingId(): Int = getPendingSystemId()
    fun publish(systemId: Int) = publishAuthorizationRequest(systemId)
    fun clear() = clearPending()

    fun getPendingSinceMs(): Long = pendingSinceMs.get()
}
