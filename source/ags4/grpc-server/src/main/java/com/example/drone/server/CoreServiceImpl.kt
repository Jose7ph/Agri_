package com.example.drone.server

import com.example.drone.ConnectionStateResponse
import com.example.drone.CoreServiceGrpc
import com.example.drone.SubscribeConnectionStateRequest
import io.grpc.StatusRuntimeException
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import java.util.UUID

class CoreServiceImpl(
    private val monitor: ConnectionStateMonitor
) : CoreServiceGrpc.CoreServiceImplBase() {

    override fun subscribeConnectionState(
        request: SubscribeConnectionStateRequest,
        responseObserver: StreamObserver<ConnectionStateResponse>
    ) {
        val serverObs = responseObserver as? ServerCallStreamObserver<ConnectionStateResponse>
        val clientId = UUID.randomUUID().toString().take(8)

        Log.i("CORE", "subscribeConnectionState() | client=$clientId subscribed")

        // Helper to write safely (avoid crashing on closed stream)
        fun safeSend(isConnected: Boolean) {
            if (serverObs?.isCancelled == true) return

            try {
                responseObserver.onNext(
                    ConnectionStateResponse.newBuilder()
                        .setIsConnected(isConnected)
                        .build()
                )
                Log.i("CORE", "push | client=$clientId isConnected=$isConnected")
            } catch (e: StatusRuntimeException) {
                Log.w("CORE", "push failed | client=$clientId status=${e.status.code}")
            } catch (e: Exception) {
                Log.w("CORE", "push failed | client=$clientId err=${e.message}")
            }
        }

        // 1) Send initial state immediately
        safeSend(monitor.isConnected())

        // 2) Push updates whenever state changes
        val listener: (Boolean) -> Unit = { isConnected ->
            safeSend(isConnected)
        }

        monitor.addListener(listener)

        // 3) Remove listener when client cancels stream
        serverObs?.setOnCancelHandler {
            monitor.removeListener(listener)
            Log.i("CORE", "cancel | client=$clientId unsubscribed")
        }
    }
}
