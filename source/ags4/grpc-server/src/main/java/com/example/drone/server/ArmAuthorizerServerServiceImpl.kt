package com.example.drone.server

import com.example.drone.*
import io.grpc.stub.StreamObserver
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ArmAuthorizerServerServiceImpl(
    private val executor: ScheduledExecutorService,
    private val monitor: ArmAuthorizationMonitor
) : ArmAuthorizerServerServiceGrpc.ArmAuthorizerServerServiceImplBase() {

    override fun subscribeArmAuthorization(
        request: SubscribeArmAuthorizationRequest,
        responseObserver: StreamObserver<ArmAuthorizationResponse>
    ) {
        val id = monitor.pendingId()

        responseObserver.onNext(
            ArmAuthorizationResponse.newBuilder()
                .setSystemid(id)
                .build()
        )
        responseObserver.onCompleted()
    }

    override fun rejectArmAuthorization(
        request: RejectArmAuthorizationRequest,
        responseObserver: StreamObserver<RejectArmAuthorizationResponse>
    ) {
        executor.schedule({
            val pending = monitor.pendingId()

            if (pending == 0) {
                responseObserver.onNext(
                    RejectArmAuthorizationResponse.newBuilder()
                        .setResult(
                            ArmAuthorizerServerResult.newBuilder()
                                .setResult(ArmAuthorizerServerResult.Result.RESULT_FAILED)
                                .setResultStr("No pending arm authorization request.")
                                .build()
                        )
                        .build()
                )
                responseObserver.onCompleted()
                return@schedule
            }

            monitor.clear()

            val res = ArmAuthorizerServerResult.newBuilder()
                .setResult(ArmAuthorizerServerResult.Result.RESULT_SUCCESS)
                .setResultStr(
                    "Rejected. temporarily=${request.temporarily}, reason=${request.reason}, extraInfo=${request.extraInfo}"
                )
                .build()

            responseObserver.onNext(
                RejectArmAuthorizationResponse.newBuilder().setResult(res).build()
            )
            responseObserver.onCompleted()
        }, 150, TimeUnit.MILLISECONDS)
    }

    override fun acceptArmAuthorization(
        request: AcceptArmAuthorizationRequest,
        responseObserver: StreamObserver<ArmAuthorizerServerResult>
    ) {
        executor.schedule({
            val pending = monitor.pendingId()

            if (pending == 0) {
                responseObserver.onNext(
                    ArmAuthorizerServerResult.newBuilder()
                        .setResult(ArmAuthorizerServerResult.Result.RESULT_FAILED)
                        .setResultStr("No pending arm authorization request.")
                        .build()
                )
                responseObserver.onCompleted()
                return@schedule
            }

            monitor.clear()

            responseObserver.onNext(
                ArmAuthorizerServerResult.newBuilder()
                    .setResult(ArmAuthorizerServerResult.Result.RESULT_SUCCESS)
                    .setResultStr(
                        "Accepted for ${request.validTimeS}s. reason=${request.reason}, extraInfo=${request.extraInfo}"
                    )
                    .build()
            )
            responseObserver.onCompleted()
        }, 150, TimeUnit.MILLISECONDS)
    }
}
