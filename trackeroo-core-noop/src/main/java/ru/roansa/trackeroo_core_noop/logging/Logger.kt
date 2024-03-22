package ru.roansa.trackeroo_core.logging

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import ru.roansa.trackeroo_core.logging.publish.ILogPublisher
import ru.roansa.trackeroo_core.logging.transform.ILogTransformer
import ru.roansa.trackeroo_core.logging.transform.LogFormatter
import ru.roansa.trackeroo_core_noop.hookers.exception.DefaultUncaughtExceptionHooker
import ru.roansa.trackeroo_core_noop.hookers.exception.UncaughtExceptionAction

object Logger {
    var logFileConfig: LogFileConfig = LogFileConfig.empty()
        private set

    fun builder(
        context: Context,
        logDirectoryName: String = "",
        logFileName: String = ""
    ): Builder {
        return Builder(context, logDirectoryName, logFileName)
    }

    fun default(applicationContext: Context) {

    }

    fun Builder.build() {

    }

    fun publish() {

    }

    fun clearLogs() {

    }

    fun setOnNewLogStringListener(block: (String) -> Unit) {

    }

    fun setOnNewLogStringListener(onNewLogStringListener: OnNewLogStringListener) {

    }

    @JvmStatic
    fun v(tag: String?, message: String): Int {
        return Log.v(tag, message)
    }

    @JvmStatic
    fun v(tag: String?, message: String?, throwable: Throwable?): Int {
        return Log.v(tag, message, throwable)
    }

    @JvmStatic
    fun d(tag: String?, message: String): Int {
        return Log.d(tag, message)
    }

    @JvmStatic
    fun d(tag: String?, message: String?, throwable: Throwable?): Int {
        return Log.d(tag, message, throwable)
    }

    @JvmStatic
    fun i(tag: String?, message: String): Int {
        return Log.i(tag, message)
    }

    @JvmStatic
    fun i(tag: String?, message: String?, throwable: Throwable?): Int {
        return Log.i(tag, message, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String): Int {
        return Log.w(tag, message)
    }

    @JvmStatic
    fun w(tag: String?, throwable: Throwable?): Int {
        return Log.w(tag, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String?, throwable: Throwable?): Int {
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun e(tag: String?, message: String): Int {
        return Log.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String?, message: String?, throwable: Throwable?): Int {
        return Log.e(tag, message, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?): Int {
        return Log.wtf(tag, message)
    }

    @JvmStatic
    fun wtf(tag: String?, throwable: Throwable): Int {
        return Log.wtf(tag, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?, throwable: Throwable?): Int {
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun log(logEntity: LogEntity): Int {
        val priority = when (logEntity.level) {
            LogLevel.VERBOSE -> Log.VERBOSE
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.WARNING -> Log.WARN
            LogLevel.INFO -> Log.INFO
            LogLevel.ERROR -> Log.ERROR
            LogLevel.ASSERT, LogLevel.UNDEFINED -> Log.ASSERT
        }
        return Log.println(priority, logEntity.tag, logEntity.message.orEmpty())
    }

    @JvmStatic
    fun println(priority: Int, tag: String?, message: String?): Int {
        return Log.println(priority, tag, message.orEmpty())
    }

    class Builder(
        internal val applicationContext: Context,
        internal var logFileConfig: LogFileConfig
    ) {
        constructor(
            context: Context,
            logDirectoryName: String = "",
            logFileName: String = ""
        ) : this(
            context,
            LogFileConfig(
                ContextWrapper(context).filesDir.toString(),
                logDirectoryName,
                logFileName
            )
        )

        internal lateinit var logWriter: ILogFileWriter
            private set
        internal lateinit var logPublisher: ILogPublisher<*>
            private set
        internal var logFormatter: LogFormatter = LogFormatter()
            private set
        internal var uncaughtExceptionHooker: DefaultUncaughtExceptionHooker =
            DefaultUncaughtExceptionHooker(applicationContext, UncaughtExceptionAction.Stub)

        fun setLogWriter(logWriter: ILogFileWriter): Builder {
            return this
        }

        fun setLogPublisher(logPublisher: ILogPublisher<*>): Builder {
            return this
        }

        fun setLogFormatter(logFormatter: LogFormatter): Builder {
            return this
        }

        fun setUncaughtExceptionHooker(uncaughtExceptionHooker: DefaultUncaughtExceptionHooker): Builder {
            return this
        }

        fun addLogTransformer(logTransformer: ILogTransformer): Builder {
            return this
        }
    }

    interface OnNewLogStringListener {
        fun onNewFormattedLogString(logString: String)
    }

}