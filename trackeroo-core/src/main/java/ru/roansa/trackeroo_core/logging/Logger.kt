package ru.roansa.trackeroo_core.logging

import android.content.Context
import android.util.Log
import ru.roansa.trackeroo_core.logging.transform.*
import ru.roansa.trackeroo_core.logging.publish.ExportLogPublisher
import ru.roansa.trackeroo_core.logging.publish.ILogPublisher
import ru.roansa.trackeroo_core.logging.file.ILogWriter
import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import ru.roansa.trackeroo_core.logging.file.LogTextFileWriter

//TODO add exceptions to necessary fields when it is not initialized
object Logger {

    private var logWriter: ILogWriter? = null
    private var logFormatter: LogFormatter? = null
    private var logPublisher: ILogPublisher? = null
    private var logFileConfig: LogFileConfig? = null

    fun init(
        context: Context,
        logDirectoryName: String = LogFileConfig.DEFAULT_DIRECTORY_NAME,
        logFileName: String = LogFileConfig.DEFAULT_FILE_NAME
    ): Builder {
        val builder = Builder(context, logDirectoryName, logFileName)
        logFileConfig = builder.logFileConfig
        return builder
    }

    fun default(context: Context) {
        init(context)
            .setLogWriter(LogTextFileWriter(logFileConfig, true))
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
    }

    fun publish() {
        logWriter?.logFileConfig?.run {
            logPublisher?.publish(logFile)
        }
    }

    fun clearLogs() {
        logWriter?.clear()
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

    class Builder(internal var logFileConfig: LogFileConfig) {

        constructor(
            context: Context,
            logDirectoryName: String = LogFileConfig.DEFAULT_DIRECTORY_NAME,
            logFileName: String = LogFileConfig.DEFAULT_FILE_NAME
        ) : this(
            LogFileConfig(
                context.filesDir.toString(),
                logDirectoryName,
                logFileName
            )
        )

        internal lateinit var logWriter: ILogWriter
            private set
        internal lateinit var logPublisher: ILogPublisher
            private set
        internal var logFormatter: LogFormatter = LogFormatter()
            private set

        fun setLogWriter(logWriter: ILogWriter): Builder {
            this.logWriter = logWriter
            return this
        }

        fun setLogPublisher(logPublisher: ILogPublisher): Builder {
            this.logPublisher = logPublisher
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