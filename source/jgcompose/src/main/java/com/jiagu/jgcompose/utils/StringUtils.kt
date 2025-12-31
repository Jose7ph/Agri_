package com.jiagu.jgcompose.utils

fun Int.toSingedString(): String {
    return if (this < 0) this.toString() else  "+${this}"
}

fun Double.toString(fraction: Int): String {
    return String.format("%.${fraction}f", this)
}

fun Float.toString(fraction: Int): String {
    return String.format("%.${fraction}f", this)
}