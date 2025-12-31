package com.jiagu.ags4.utils

val curve = floatArrayOf(3f, 3.68f, 3.74f, 3.77f, 3.79f, 3.82f, 3.87f, 3.92f, 3.98f, 4.06f, 4.2f)
fun volt2percent(volt: Float): Float {
    var idx = curve.binarySearch(volt)
    return if (idx >= 0) if (idx <= 10) idx / 10f else 1f
    else when (idx) {
        -1 -> 0f
        -12 -> 1f
        else -> {
            idx = -(idx + 1)
            val y = (volt - curve[idx - 1]) / (curve[idx] - curve[idx - 1])
            (idx - 1 + y) / 10f
        }
    }
}

fun transRadarWarnLevel(level: Int): Int {
    return when (level) {
        0 -> -1
        in 1..9 -> 0
        in 10..12 -> 1
        in 12..16 -> 2
        else -> 3
    }
}