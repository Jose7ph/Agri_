package com.example.drone.server

import com.example.drone.*
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver

class FlightControllerServiceImpl(
    private val fc: FlightControllerMonitor
) : FlightControllerServiceGrpc.FlightControllerServiceImplBase() {

    override fun subscribeGpsInfo(
        request: SubscribeGpsInfoRequest,
        responseObserver: StreamObserver<GpsInfoResponse>
    ) {
        val serverObs = responseObserver as? ServerCallStreamObserver<GpsInfoResponse>

        // 1) Send initial state
        responseObserver.onNext(
            GpsInfoResponse.newBuilder()
                .setGpsInfo(fc.getGpsInfo())
                .build()
        )

        // 2) Listener for updates
        val listener: (GpsInfo) -> Unit = { info ->
            if (serverObs?.isCancelled == true) {
                // ignore
            } else {
                responseObserver.onNext(
                    GpsInfoResponse.newBuilder()
                        .setGpsInfo(info)
                        .build()
                )
            }
        }

        fc.onGps(listener)

        // 3) Cleanup on cancel
        serverObs?.setOnCancelHandler {
            fc.offGps(listener)
        }
    }
}
