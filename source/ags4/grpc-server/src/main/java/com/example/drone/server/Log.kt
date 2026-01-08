package com.example.drone.server

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Log {
    private val fmt = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    fun i(tag: String, msg: String) {
        val ts = LocalDateTime.now().format(fmt)
        println("$ts | ${tag.padEnd(4)} | $msg")
    }

    fun w(tag: String, msg: String) {
        val ts = LocalDateTime.now().format(fmt)
        println("$ts | ${tag.padEnd(4)} | WARN: $msg")
    }

    fun e(tag: String, msg: String) {
        val ts = LocalDateTime.now().format(fmt)
        println("$ts | ${tag.padEnd(4)} | ERROR: $msg")
    }
}
