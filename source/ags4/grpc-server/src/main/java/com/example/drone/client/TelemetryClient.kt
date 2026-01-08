package com.example.drone.client

import com.example.drone.*
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun main() {
    val channel = ManagedChannelBuilder
        .forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    val stub = TelemetryServiceGrpc.newStub(channel)
    val latch = CountDownLatch(1)

    stub.subscribePosition(
        SubscribePositionRequest.newBuilder().build(),
        object : StreamObserver<PositionResponse> {
            override fun onNext(value: PositionResponse) {
                val p = value.position
                println("POS: lat=${p.latitudeDeg}, lon=${p.longitudeDeg}, absAlt=${p.absoluteAltitudeM}, relAlt=${p.relativeAltitudeM}")
            }
            override fun onError(t: Throwable) { println("POS error: ${t.message}") }
            override fun onCompleted() { println("POS completed") }
        }
    )

    stub.subscribeAttitudeEuler(
        SubscribeAttitudeEulerRequest.newBuilder().build(),
        object : StreamObserver<AttitudeEulerResponse> {
            override fun onNext(value: AttitudeEulerResponse) {
                println("ATT: roll=${value.rollDeg}, pitch=${value.pitchDeg}, yaw=${value.yawDeg}, ts=${value.timeStampsUs}")
            }
            override fun onError(t: Throwable) { println("ATT error: ${t.message}") }
            override fun onCompleted() { println("ATT completed") }
        }
    )

    latch.await(10, TimeUnit.MINUTES)
    channel.shutdownNow()
}
