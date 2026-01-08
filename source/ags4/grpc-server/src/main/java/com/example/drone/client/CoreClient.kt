package com.example.drone.client

import com.example.drone.ConnectionStateResponse
import com.example.drone.CoreServiceGrpc
import com.example.drone.SubscribeConnectionStateRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun main() {
    val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    val stub = CoreServiceGrpc.newStub(channel)

    val latch = CountDownLatch(1)

    stub.subscribeConnectionState(
        SubscribeConnectionStateRequest.newBuilder().build(),
        object : StreamObserver<ConnectionStateResponse> {
            override fun onNext(value: ConnectionStateResponse) {
                println("Connection state => isConnected=${value.isConnected}")
            }

            override fun onError(t: Throwable) {
                println("Stream error: ${t.message}")
                latch.countDown()
            }

            override fun onCompleted() {
                println("Stream completed")
                latch.countDown()
            }
        }
    )

    // Keep process alive to receive stream updates
    latch.await(10, TimeUnit.MINUTES)

    channel.shutdownNow()
}
