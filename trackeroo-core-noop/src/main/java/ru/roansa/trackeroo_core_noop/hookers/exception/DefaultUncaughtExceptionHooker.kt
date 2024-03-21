package ru.roansa.trackeroo_core_noop.hookers.exception

import android.content.Context

open class DefaultUncaughtExceptionHooker(
    private val context: Context,
    private val actionOnException: UncaughtExceptionAction
) : ExceptionHooker, Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, exception: Throwable) {}
}

sealed class UncaughtExceptionAction {
    object Stub : UncaughtExceptionAction()
}