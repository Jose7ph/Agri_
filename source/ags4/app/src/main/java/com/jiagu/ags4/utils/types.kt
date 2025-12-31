package com.jiagu.ags4.utils

import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapBlock
import com.jiagu.api.model.MapBlock3D
import com.jiagu.api.model.MapRing
import com.jiagu.api.model.MapRing3D
import com.jiagu.api.model.mapRing3DToArray
import com.jiagu.device.model.RoutePoint

fun arrayToMapRing(data: DoubleArray): MapRing {
    val out = mutableListOf<GeoHelper.LatLng>()
    for (idx in data.indices step 2) {
        out.add(GeoHelper.LatLng(data[idx], data[idx + 1]))
    }
    return out
}

fun arrayToMapRing3D(data: DoubleArray, alt: DoubleArray?): MapRing3D {
    val out = mutableListOf<GeoHelper.LatLngAlt>()
    for (idx in data.indices step 2) {
        val h = if (alt == null || alt.isEmpty() || (idx / 2 >= alt.size)) 0.0 else alt[idx / 2]
        out.add(GeoHelper.LatLngAlt(data[idx], data[idx + 1], h))
    }
    return out
}

fun mergeAlt(ring: MapRing, alt: DoubleArray?): MapRing3D {
    val out = mutableListOf<GeoHelper.LatLngAlt>()
    for ((idx, pt) in ring.withIndex()) {
        val h = if (alt == null || alt.isEmpty()) 0.0 else alt[idx]
        out.add(GeoHelper.LatLngAlt(pt.latitude, pt.longitude, h))
    }
    return out
}

fun mapRingToArray(data: MapRing): DoubleArray {
    val out = mutableListOf<Double>()
    for (pt in data) {
        out.add(pt.latitude)
        out.add(pt.longitude)
    }
    return out.toDoubleArray()
}

fun toAltArray(ring: MapRing3D): DoubleArray {
    val out = DoubleArray(ring.size)
    for (i in ring.indices) {
        out[i] = ring[i].altitude
    }
    return out
}

fun arrayToMapBlock(data: Array<DoubleArray>?): MapBlock {
    if (data == null) return listOf()
    val out = mutableListOf<MapRing>()
    for ((i, ring) in data.withIndex()) {
        if (i == 0 || ring.isNotEmpty()) {
            out.add(arrayToMapRing(ring))
        }
    }
    return out
}

fun mapBlockToArray(data: MapBlock): Array<DoubleArray> {
    val out = mutableListOf<DoubleArray>()
    for ((i, ring) in data.withIndex()) {
        if (i == 0 || ring.isNotEmpty()) {
            out.add(mapRingToArray(ring))
        }
    }
    return out.toTypedArray()
}

fun arrayToMapBlock3D(data: Array<DoubleArray>?, alt: Array<DoubleArray>?): MapBlock3D {
    if (data == null) return listOf()
    val out = mutableListOf<MapRing3D>()
    for ((i, ring) in data.withIndex()) {
        if (i == 0 || ring.isNotEmpty()) {
            val h = if (alt == null || i >= alt.size) null else alt[i]
            out.add(arrayToMapRing3D(ring, h))
        }
    }
    return out
}

fun mapBlock3DToArray(data: MapBlock3D): Pair<Array<DoubleArray>, Array<DoubleArray>> {
    val boundary = mutableListOf<DoubleArray>()
    val altitude = mutableListOf<DoubleArray>()
    for (b in data) {
        boundary.add(mapRingToArray(b))
        altitude.add(toAltArray(b))
    }
    return Pair(boundary.toTypedArray(), altitude.toTypedArray())
}

fun arrayToBarrier(data: Array<DoubleArray>?, alt: Array<DoubleArray>?): MapBlock3D {
    if (data == null || data.size < 2) return listOf()
    val out = mutableListOf<MapRing3D>()
    for (i in 1 until data.size) {
        val ring = data.get(i)
        val h = if (alt == null || i >= alt.size) doubleArrayOf() else alt.get(i)
        out.add(arrayToMapRing3D(ring, h))
    }
    return out
}

fun map3DToBarrier(data: MapBlock3D): MapBlock3D {
    val out = mutableListOf<MapRing3D>()
    for (i in 1 until data.size) {
        out.add(data[i])
    }
    return out
}

fun arrayToRoutePoint(data: Array<String>): List<RoutePoint> {
    val out = mutableListOf<RoutePoint>()
    for (s in data) {
        val rp = RoutePoint.fromString(s)
        out.add(rp)
    }
    return out
}

fun routePointToArray(data: List<RoutePoint>): Array<String> {
    val out = mutableListOf<String>()
    for (p in data) {
        out.add(p.toString())
    }
    return out.toTypedArray()
}

fun arrayToCalib(data: DoubleArray?): List<GeoHelper.LatLngAlt> {
    if (data == null) return listOf()
    val out = mutableListOf<GeoHelper.LatLngAlt>()
    for (i in data.indices step 3) {
        out.add(GeoHelper.LatLngAlt(data[i], data[i + 1], data[i + 2]))
    }
    return out
}

fun calibToArray(data: List<GeoHelper.LatLngAlt>): DoubleArray {
    val out = mutableListOf<Double>()
    for (v in data) {
        out.add(v.latitude)
        out.add(v.longitude)
        out.add(v.altitude)
    }
    return out.toDoubleArray()
}
