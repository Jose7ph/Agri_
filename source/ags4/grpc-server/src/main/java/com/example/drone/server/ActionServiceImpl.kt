package com.example.drone.server

import com.example.drone.*
import io.grpc.stub.StreamObserver
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit



class ActionServiceImpl(
    private val executor: ScheduledExecutorService,
    private val monitor: ConnectionStateMonitor,
    private val telemetry: TelemetryMonitor,
    private val fcMonitor: FlightControllerMonitor,
    private val actionState: ActionState
) : ActionServiceGrpc.ActionServiceImplBase() {

    init {
        Log.i("ACT", "ActionServiceImpl REGISTERED")
    }
    override fun hold(request: HoldRequest, responseObserver: StreamObserver<HoldResponse>) {
        executor.schedule({

            // Preconditions (demo-realistic)
            if (!monitor.isConnected()) {
                respond(responseObserver, Result.RESULT_COMMAND_DENIED, "Denied: No UAV connection.")
                return@schedule
            }
            if (!telemetry.getInAir()) {
                respond(responseObserver, Result.RESULT_COMMAND_DENIED, "Denied: Vehicle is not in-air.")
                return@schedule
            }

            val gps = fcMonitor.getGpsInfo()
            if (gps.signalLevel < GpsSignalLevel.LEVEL_3) {
                respond(
                    responseObserver,
                    Result.RESULT_COMMAND_DENIED,
                    "Denied: GPS too weak for HOLD (need LEVEL_3+)."
                )
                return@schedule
            }

            // Apply HOLD in demo (freeze drift)
            actionState.setHold(true)

            respond(responseObserver, Result.RESULT_SUCCESS, "Hold activated.")
        }, 200, TimeUnit.MILLISECONDS)
    }

    private fun respond(
        responseObserver: StreamObserver<HoldResponse>,
        result: Result,
        msg: String
    ) {
        val actionResult = ActionResult.newBuilder()
            .setResult(result)
            .setResultStr(msg)
            .build()

        responseObserver.onNext(
            HoldResponse.newBuilder().setActionResult(actionResult).build()
        )
        responseObserver.onCompleted()
    }
}
