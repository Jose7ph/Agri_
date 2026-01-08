package com.example.drone.server

import com.example.drone.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class TelemetryMonitor {

    private val position = AtomicReference(
        Position.newBuilder()
            .setLatitudeDeg(36.8121)     // demo (Mersin-ish)
            .setLongitudeDeg(34.6415)
            .setAbsoluteAltitudeM(120.0f)
            .setRelativeAltitudeM(15.0f)
            .build()
    )

    private val altitude = AtomicReference(
        Altitude.newBuilder()
            .setAltitudeAmslM(120.0f)
            .build()
    )

    private val inAir = AtomicReference(false)

    private val attitude = AtomicReference(
        AttitudeEulerResponse.newBuilder()
            .setRollDeg(0f)
            .setPitchDeg(0f)
            .setYawDeg(0f)
            .setTimeStampsUs(System.nanoTime() / 1000)
            .build()
    )

    private val positionListeners = CopyOnWriteArrayList<(Position) -> Unit>()
    private val altitudeListeners = CopyOnWriteArrayList<(Altitude) -> Unit>()
    private val inAirListeners = CopyOnWriteArrayList<(Boolean) -> Unit>()
    private val attitudeListeners = CopyOnWriteArrayList<(AttitudeEulerResponse) -> Unit>()

    fun getPosition(): Position = position.get()
    fun getAltitude(): Altitude = altitude.get()
    fun getInAir(): Boolean = inAir.get()
    fun getAttitude(): AttitudeEulerResponse = attitude.get()

    fun setPosition(value: Position) {
        position.set(value)
        positionListeners.forEach { it(value) }
    }

    fun setAltitude(value: Altitude) {
        altitude.set(value)
        altitudeListeners.forEach { it(value) }
    }

    fun setInAir(value: Boolean) {
        inAir.set(value)
        inAirListeners.forEach { it(value) }
    }

    fun setAttitude(value: AttitudeEulerResponse) {
        attitude.set(value)
        attitudeListeners.forEach { it(value) }
    }

    fun onPosition(listener: (Position) -> Unit) = positionListeners.add(listener)
    fun offPosition(listener: (Position) -> Unit) = positionListeners.remove(listener)

    fun onAltitude(listener: (Altitude) -> Unit) = altitudeListeners.add(listener)
    fun offAltitude(listener: (Altitude) -> Unit) = altitudeListeners.remove(listener)

    fun onInAir(listener: (Boolean) -> Unit) = inAirListeners.add(listener)
    fun offInAir(listener: (Boolean) -> Unit) = inAirListeners.remove(listener)

    fun onAttitude(listener: (AttitudeEulerResponse) -> Unit) = attitudeListeners.add(listener)
    fun offAttitude(listener: (AttitudeEulerResponse) -> Unit) = attitudeListeners.remove(listener)



}

