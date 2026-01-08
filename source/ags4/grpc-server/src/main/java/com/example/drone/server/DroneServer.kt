package com.example.drone.server

import com.example.drone.*
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/* ============================================================
   gRPC SERVICE: DroneService (Demo)
   ============================================================ */
class DroneServiceImpl : DroneServiceGrpc.DroneServiceImplBase() {

    override fun streamDrones(
        request: DroneRequest,
        responseObserver: StreamObserver<DroneInfo>
    ) {
        Log.i("GRPC", "Drone list requested | clientId=${request.clientId}")

        val drones = listOf(
            DroneInfo.newBuilder().setId("1").setName("CT-33").build(),
            DroneInfo.newBuilder().setId("2").setName("CT-70").build(),
            DroneInfo.newBuilder().setId("3").setName("CT-110").build()
        )

        drones.forEach { responseObserver.onNext(it) }
        responseObserver.onCompleted()

        Log.i("GRPC", "Drone list streamed | count=${drones.size}")
    }
}

/* ============================================================
   APPLICATION ENTRY POINT
   ============================================================ */
fun main() {
    val port = 50051

    Log.i("SYS", "============================================================")
    Log.i("SYS", " Demo gRPC Server")
    Log.i("SYS", " Host=0.0.0.0  Port=$port  Transport=gRPC(HTTP/2)")
    Log.i("SYS", "============================================================")

    // Shared state
    val connectionMonitor = ConnectionStateMonitor(initial = true) // stable TRUE for demo
    val telemetryMonitor = TelemetryMonitor()
    val fcMonitor = FlightControllerMonitor()
    val armMonitor = ArmAuthorizationMonitor()
    val actionState = ActionState()

    // Executors
    val actionExecutor = Executors.newSingleThreadScheduledExecutor()
    val armExecutor = Executors.newSingleThreadScheduledExecutor()

    val telemetryScheduler = Executors.newSingleThreadScheduledExecutor()
    val gpsScheduler = Executors.newSingleThreadScheduledExecutor()
    val armScheduler = Executors.newSingleThreadScheduledExecutor()
    val heartbeatScheduler = Executors.newSingleThreadScheduledExecutor()

    Log.i("CORE", "ConnectionState initialized | isConnected=${connectionMonitor.isConnected()} (stable)")

    // gRPC Server
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(DroneServiceImpl())
        .addService(CoreServiceImpl(connectionMonitor))
        .addService(TelemetryServiceImpl(telemetryMonitor))
        .addService(FlightControllerServiceImpl(fcMonitor))
        .addService(ActionServiceImpl(actionExecutor, connectionMonitor, telemetryMonitor, fcMonitor, actionState))
        .addService(ArmAuthorizerServerServiceImpl(armExecutor, armMonitor))
        .addService(InfoServiceImpl(connectionMonitor))
        .build()
        .start()

    Log.i("GRPC", "Server started | port=$port")

    // Demo loops
    startTelemetryDemo(telemetryScheduler, telemetryMonitor, actionState)
    startGpsDemo(gpsScheduler, fcMonitor)
    startArmAuthDemo(armScheduler, armMonitor)
    startHeartbeat(heartbeatScheduler, connectionMonitor, telemetryMonitor, fcMonitor, armMonitor, actionState)

    Runtime.getRuntime().addShutdownHook(Thread {
        Log.w("SYS", "Shutdown initiated...")

        shutdownQuietly(heartbeatScheduler, "SYS", "heartbeatScheduler")
        shutdownQuietly(telemetryScheduler, "SYS", "telemetryScheduler")
        shutdownQuietly(gpsScheduler, "SYS", "gpsScheduler")
        shutdownQuietly(armScheduler, "SYS", "armScheduler")

        shutdownQuietly(actionExecutor, "SYS", "actionExecutor")
        shutdownQuietly(armExecutor, "SYS", "armExecutor")

        server.shutdown()
        Log.w("SYS", "gRPC server shutdown called.")
    })

    server.awaitTermination()
}

/* ============================================================
   DEMO HELPERS
   ============================================================ */

private fun startTelemetryDemo(
    scheduler: ScheduledExecutorService,
    telemetryMonitor: TelemetryMonitor,
    actionState: ActionState
) {
    Log.i("TEL", "Telemetry demo started | rate=1Hz | HOLD affects drift")

    var lastHold: Boolean? = null

    scheduler.scheduleAtFixedRate({
        val holdEnabled = actionState.isHoldEnabled()

        if (lastHold == null || lastHold != holdEnabled) {
            Log.i("ACT", "HOLD state changed | holdEnabled=$holdEnabled")
            lastHold = holdEnabled
        }

        telemetryMonitor.setInAir(true) // demo: keep true so HOLD can work

        val oldPos = telemetryMonitor.getPosition()
        val newPos = if (!holdEnabled) {
            oldPos.toBuilder()
                .setLatitudeDeg(oldPos.latitudeDeg + 0.00001)
                .setLongitudeDeg(oldPos.longitudeDeg + 0.00001)
                .setAbsoluteAltitudeM(oldPos.absoluteAltitudeM + 0.2f)
                .setRelativeAltitudeM(oldPos.relativeAltitudeM + 0.2f)
                .build()
        } else {
            oldPos
        }

        telemetryMonitor.setPosition(newPos)

        telemetryMonitor.setAltitude(
            telemetryMonitor.getAltitude().toBuilder()
                .setAltitudeAmslM(newPos.absoluteAltitudeM)
                .build()
        )

        telemetryMonitor.setAttitude(
            AttitudeEulerResponse.newBuilder()
                .setRollDeg(if (holdEnabled) (0..2).random().toFloat() else (0..10).random().toFloat())
                .setPitchDeg(if (holdEnabled) (0..2).random().toFloat() else (0..10).random().toFloat())
                .setYawDeg((0..360).random().toFloat())
                .setTimeStampsUs(System.nanoTime() / 1_000)
                .build()
        )
    }, 0, 1, TimeUnit.SECONDS)
}

private fun startGpsDemo(
    scheduler: ScheduledExecutorService,
    fcMonitor: FlightControllerMonitor
) {
    Log.i("GPS", "GPS demo started | rate=0.5Hz (2s) | logs on change only")

    var lastSat: Int? = null
    var lastLevel: GpsSignalLevel? = null

    scheduler.scheduleAtFixedRate({
        val satellites = (0..18).random()
        val level = when {
            satellites <= 1 -> GpsSignalLevel.LEVEL_0
            satellites <= 3 -> GpsSignalLevel.LEVEL_1
            satellites <= 6 -> GpsSignalLevel.LEVEL_2
            satellites <= 9 -> GpsSignalLevel.LEVEL_3
            satellites <= 12 -> GpsSignalLevel.LEVEL_4
            else -> GpsSignalLevel.LEVEL_5
        }

        fcMonitor.setGpsInfo(
            GpsInfo.newBuilder()
                .setNumSatellites(satellites)
                .setSignalLevel(level)
                .build()
        )

        if (lastSat != satellites || lastLevel != level) {
            Log.i("GPS", "Updated | sat=$satellites level=${level.name}")
            lastSat = satellites
            lastLevel = level
        }
    }, 0, 2, TimeUnit.SECONDS)
}

private fun startArmAuthDemo(
    scheduler: ScheduledExecutorService,
    armMonitor: ArmAuthorizationMonitor
) {
    Log.i("ARM", "ArmAuth demo started | every 10s create request if none pending")

    scheduler.scheduleAtFixedRate({
        val pending = armMonitor.getPendingSystemId()
        if (pending == 0) {
            val systemId = 1
            armMonitor.publishAuthorizationRequest(systemId)
            Log.i("ARM", "REQUEST created | systemId=$systemId")
        } else {
            Log.i("ARM", "Pending already exists | systemId=$pending")
        }
    }, 2, 10, TimeUnit.SECONDS)
}

private fun startHeartbeat(
    scheduler: ScheduledExecutorService,
    connectionMonitor: ConnectionStateMonitor,
    telemetryMonitor: TelemetryMonitor,
    fcMonitor: FlightControllerMonitor,
    armMonitor: ArmAuthorizationMonitor,
    actionState: ActionState
) {
    val started = AtomicBoolean(false)

    scheduler.scheduleAtFixedRate({
        if (!started.getAndSet(true)) {
            Log.i("SYS", "Heartbeat enabled | rate=0.2Hz (5s)")
        }

        val gps = fcMonitor.getGpsInfo()
        val pendingArm = armMonitor.getPendingSystemId()
        val pos = telemetryMonitor.getPosition()

        Log.i(
            "STAT",
            "conn=${connectionMonitor.isConnected()} inAir=${telemetryMonitor.getInAir()} " +
                    "gps=${gps.numSatellites}/${gps.signalLevel.name} " +
                    "hold=${actionState.isHoldEnabled()} armPending=$pendingArm " +
                    "lat=%.5f lon=%.5f alt=%.1f".format(pos.latitudeDeg, pos.longitudeDeg, pos.absoluteAltitudeM)
        )
    }, 5, 5, TimeUnit.SECONDS)
}

private fun shutdownQuietly(exec: ExecutorService, tag: String, name: String) {
    try {
        exec.shutdownNow()
        Log.w(tag, "Stopped $name")
    } catch (e: Exception) {
        Log.e(tag, "Failed stopping $name: ${e.message}")
    }
}
