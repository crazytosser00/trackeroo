package ru.roansa.trackeroo_core.logging

import android.content.Context
import android.util.Log
import ru.roansa.trackeroo_core.logging.transform.*
import ru.roansa.trackeroo_core.publish.ExportLogPublisher
import ru.roansa.trackeroo_core.publish.ILogPublisher
import kotlin.math.log

object Logger {

    private var logWriter: ILogWriter? = null
    private var logFormatter: LogFormatter? = null
    private var logPublisher: ILogPublisher? = null

    //TODO move file data to new class
    private var logFileName: String = "log.txt"

    fun init(): Builder = Builder()

    fun default(context: Context) {
        logPublisher = ExportLogPublisher(context)
        init()
            .setLogWriter(LogFileWriter("trackeroo", logFileName, context, true))
            .setLogPublisher(ExportLogPublisher(context))
            .addLogTransformer(TimeTransformer())
            .addLogTransformer(DebugLevelTransformer())
            .addLogTransformer(MessageTransformer())
            .build()
    }

    fun Builder.build() {
        this@Logger.logWriter = logWriter
        this@Logger.logFormatter = logFormatter
        this@Logger.logPublisher = logPublisher
        logFileName?.run { this@Logger.logFileName = this }
    }

    fun publish() {
        logWriter?.run {
            logPublisher?.publish(logFile)
        }
    }

    @JvmStatic
    fun v(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.VERBOSE, tag, message))
        logWriter?.write(logString ?: "")
        return Log.v(tag, message)
    }

    @JvmStatic
    fun v(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString =
            logFormatter?.transform(LogEntity(LogLevel.VERBOSE, tag, message, throwable))
        logWriter?.write(logString ?: "")
        return Log.v(tag, message, throwable)
    }

    @JvmStatic
    fun d(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.DEBUG, tag, message))
        logWriter?.write(logString ?: "")
        return Log.d(tag, message)
    }

    @JvmStatic
    fun d(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.DEBUG, tag, message, throwable))
        logWriter?.write(logString ?: "")
        return Log.d(tag, message, throwable)
    }

    @JvmStatic
    fun i(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.INFO, tag, message))
        logWriter?.write(logString ?: "")
        return Log.i(tag, message)
    }

    @JvmStatic
    fun i(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.INFO, tag, message, throwable))
        logWriter?.write(logString ?: "")
        return Log.i(tag, message, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, message))
        logWriter?.write(logString ?: "")
        return Log.w(tag, message)
    }

    @JvmStatic
    fun w(tag: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, null, throwable))
        logWriter?.write(logString ?: "")
        return Log.w(tag, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString =
            logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, message, throwable))
        logWriter?.write(logString ?: "")
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun e(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ERROR, tag, message))
        logWriter?.write(logString ?: "")
        return Log.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ERROR, tag, message, throwable))
        logWriter?.write(logString ?: "")
        return Log.e(tag, message, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message))
        logWriter?.write(logString ?: "")
        return Log.wtf(tag, message)
    }

    @JvmStatic
    fun wtf(tag: String?, throwable: Throwable): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, null, throwable))
        logWriter?.write(logString ?: "")
        return Log.wtf(tag, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message, throwable))
        logWriter?.write(logString ?: "")
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun println(priority: Int, tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message))
        logWriter?.write(logString ?: "")
        return Log.println(priority, tag, message)
    }

    class Builder {

        lateinit var logWriter: ILogWriter
            private set
        lateinit var logPublisher: ILogPublisher
            private set
        var logFormatter: LogFormatter = LogFormatter()
            private set
        var logFileName: String? = null


        fun setLogWriter(logWriter: ILogWriter): Builder {
            this.logWriter = logWriter
            return this
        }

        fun setLogPublisher(logPublisher: ILogPublisher): Builder {
            this.logPublisher = logPublisher
            return this
        }

        fun setLogFileName(logFileName: String): Builder {
            this.logFileName = logFileName
            return this
        }

        fun setLogFormatter(logFormatter: LogFormatter): Builder {
            this.logFormatter = logFormatter
            return this
        }

        fun addLogTransformer(logTransformer: ILogTransformer): Builder {
            logFormatter.applyTransformer(logTransformer)
            return this
        }

    }

}