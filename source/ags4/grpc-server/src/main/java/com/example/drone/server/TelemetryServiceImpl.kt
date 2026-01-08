package com.example.drone.server

import com.example.drone.*
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver

class TelemetryServiceImpl(
    private val telemetry: TelemetryMonitor
) : TelemetryServiceGrpc.TelemetryServiceImplBase() {

    override fun subscribePosition(
        request: SubscribePositionRequest,
        responseObserver: StreamObserver<PositionResponse>
    ) {
        val serverObs = responseObserver as? ServerCallStreamObserver<PositionResponse>

        // initial
        responseObserver.onNext(
            PositionResponse.newBuilder().setPosition(telemetry.getPosition()).build()
        )

        val listener: (Position) -> Unit = { pos ->
            if (serverObs?.isCancelled == true) {
                // ignore
            } else {
                responseObserver.onNext(
                    PositionResponse.newBuilder().setPosition(pos).build()
                )
            }
        }

        telemetry.onPosition(listener)
        serverObs?.setOnCancelHandler { telemetry.offPosition(listener) }
    }

    override fun subscribeAltitude(
        request: SubscribeAltitudeRequest,
        responseObserver: StreamObserver<AltitudeResponse>
    ) {
        val serverObs = responseObserver as? ServerCallStreamObserver<AltitudeResponse>

        responseObserver.onNext(
            AltitudeResponse.newBuilder().setAltitude(telemetry.getAltitude()).build()
        )

        val listener: (Altitude) -> Unit = { alt ->
            if (serverObs?.isCancelled == true) {
                // ignore
            } else {
                responseObserver.onNext(
                    AltitudeResponse.newBuilder().setAltitude(alt).build()
                )
            }
        }

        telemetry.onAltitude(listener)
        serverObs?.setOnCancelHandler { telemetry.offAltitude(listener) }
    }

    override fun subscribeInAir(
        request: SubscribeInAirRequest,
        responseObserver: StreamObserver<InAirResponse>
    ) {
        val serverObs = responseObserver as? ServerCallStreamObserver<InAirResponse>

        responseObserver.onNext(
            InAirResponse.newBuilder().setIsInAir(telemetry.getInAir()).build()
        )

        val listener: (Boolean) -> Unit = { isInAir ->
            if (serverObs?.isCancelled == true) {
                // ignore
            } else {
                responseObserver.onNext(
                    InAirResponse.newBuilder().setIsInAir(isInAir).build()
                )
            }
        }

        telemetry.onInAir(listener)
        serverObs?.setOnCancelHandler { telemetry.offInAir(listener) }
    }

    override fun subscribeAttitudeEuler(
        request: SubscribeAttitudeEulerRequest,
        responseObserver: StreamObserver<AttitudeEulerResponse>
    ) {
        val serverObs = responseObserver as? ServerCallStreamObserver<AttitudeEulerResponse>

        responseObserver.onNext(telemetry.getAttitude())

        val listener: (AttitudeEulerResponse) -> Unit = { att ->
            if (serverObs?.isCancelled == true) {
                // ignore
            } else {
                responseObserver.onNext(att)
            }
        }

        telemetry.onAttitude(listener)
        serverObs?.setOnCancelHandler { telemetry.offAttitude(listener) }
    }
}
