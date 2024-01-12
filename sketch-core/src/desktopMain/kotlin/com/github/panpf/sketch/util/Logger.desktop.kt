package com.github.panpf.sketch.util

import java.io.ByteArrayOutputStream
import java.io.PrintStream

actual fun logProxy(): Logger.Proxy = JvmLogProxy()

class JvmLogProxy : Logger.Proxy {
    override fun v(tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = stackTraceToString(tr)
            println("VERBOSE. $tag. $msg. $trString")
        } else {
            println("VERBOSE. $tag. $msg")
        }
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = stackTraceToString(tr)
            println("DEBUG. $tag. $msg. $trString")
        } else {
            println("DEBUG. $tag. $msg")
        }
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = stackTraceToString(tr)
            println("INFO. $tag. $msg. $trString")
        } else {
            println("INFO. $tag. $msg")
        }
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = stackTraceToString(tr)
            println("WARNING. $tag. $msg. $trString")
        } else {
            println("WARNING. $tag. $msg")
        }
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        if (tr != null) {
            val trString = stackTraceToString(tr)
            println("ERROR. $tag. $msg. $trString")
        } else {
            println("ERROR. $tag. $msg")
        }
    }

    override fun flush() {

    }

    private fun stackTraceToString(throwable: Throwable): String {
        val arrayOutputStream = ByteArrayOutputStream()
        val printWriter = PrintStream(arrayOutputStream)
        throwable.printStackTrace(printWriter)
        return String(arrayOutputStream.toByteArray())
    }

    override fun toString(): String = "JvmLogProxy"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}