package ru.roansa.trackeroo_core.logging

import android.content.Context
import android.util.Log
import ru.roansa.trackeroo_core.hookers.ViewHooker
import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import ru.roansa.trackeroo_core.logging.file.text.LogTextFileWriter
import ru.roansa.trackeroo_core.logging.publish.ILogPublisher
import ru.roansa.trackeroo_core.logging.publish.ShareIntentLogPublisher
import ru.roansa.trackeroo_core.logging.transform.DebugLevelTransformer
import ru.roansa.trackeroo_core.logging.transform.ILogTransformer
import ru.roansa.trackeroo_core.logging.transform.LogFormatter
import ru.roansa.trackeroo_core.logging.transform.MessageTransformer
import ru.roansa.trackeroo_core.logging.transform.TimeTransformer

//TODO add exceptions to necessary fields when it is not initialized
//TODO add methods like default logging methods (like w(), d() etc.) but with parameters variable as parameter
object Logger {

    const val TAG: String = "Trackeroo-Logger"
    private var logWriter: ILogFileWriter? = null
    private var logFormatter: LogFormatter? = null
    private var logPublisher: ILogPublisher<*>? = null
    private var onNewLogStringListener: OnNewLogStringListener? = null
    var logFileConfig: LogFileConfig = LogFileConfig.empty()
        private set

    fun builder(
        context: Context,
        logDirectoryName: String = LogFileConfig.DEFAULT_DIRECTORY_NAME,
        logFileName: String = LogFileConfig.DEFAULT_FILE_NAME
    ): Builder {
        val builder = Builder(context, logDirectoryName, logFileName)
        logFileConfig = builder.logFileConfig
        return builder
    }

    fun default(applicationContext: Context) {
        builder(applicationContext)
            .setLogWriter(LogTextFileWriter(logFileConfig, true))
            .setLogPublisher(ShareIntentLogPublisher(applicationContext))
            .addLogTransformer(TimeTransformer())
            .addLogTransformer(DebugLevelTransformer())
            .addLogTransformer(MessageTransformer())
            .build()
    }

    fun Builder.build() {
        ViewHooker.init(this.applicationContext)
        this@Logger.logWriter = logWriter
        this@Logger.logFormatter = logFormatter
        this@Logger.logPublisher = logPublisher
        this@Logger.logWriter?.let {
            this@Logger.logPublisher?.setLogWriter(it)
        }
    }

    fun publish(): Any? =
        logFileConfig.run {
            logPublisher?.publish()
        }

    fun clearLogs() {
        logWriter?.clear()
    }

    fun setOnNewLogStringListener(block: (String) -> Unit) {
        onNewLogStringListener = object : OnNewLogStringListener {
            override fun onNewFormattedLogString(logString: String) {
                block(logString)
            }
        }
    }

    fun setOnNewLogStringListener(onNewLogStringListener: OnNewLogStringListener) {
        this.onNewLogStringListener = onNewLogStringListener
    }

    @JvmStatic
    fun v(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.VERBOSE, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.v(tag, message)
    }

    @JvmStatic
    fun v(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString =
            logFormatter?.transform(LogEntity(LogLevel.VERBOSE, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.v(tag, message, throwable)
    }

    @JvmStatic
    fun d(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.DEBUG, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.d(tag, message)
    }

    @JvmStatic
    fun d(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.DEBUG, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.d(tag, message, throwable)
    }

    @JvmStatic
    fun i(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.INFO, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.i(tag, message)
    }

    @JvmStatic
    fun i(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.INFO, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.i(tag, message, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.w(tag, message)
    }

    @JvmStatic
    fun w(tag: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, null, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.w(tag, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString =
            logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun e(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ERROR, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ERROR, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.e(tag, message, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.wtf(tag, message)
    }

    @JvmStatic
    fun wtf(tag: String?, throwable: Throwable): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, null, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.wtf(tag, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun log(logEntity: LogEntity): Int {
        val logString = logFormatter?.transform(logEntity)
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
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
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message))
        logWriter?.write(logString)
        logString?.let { onNewLogStringListener?.onNewFormattedLogString(it) }
        return Log.println(priority, tag, message.orEmpty())
    }

    class Builder(
        internal val applicationContext: Context,
        internal var logFileConfig: LogFileConfig
    ) {

        constructor(
            context: Context,
            logDirectoryName: String = LogFileConfig.DEFAULT_DIRECTORY_NAME,
            logFileName: String = LogFileConfig.DEFAULT_FILE_NAME
        ) : this(
            context,
            LogFileConfig(
                context.filesDir.toString(),
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

        fun setLogWriter(logWriter: ILogFileWriter): Builder {
            this.logWriter = logWriter
            return this
        }

        fun setLogPublisher(logPublisher: ILogPublisher<*>): Builder {
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

    interface OnNewLogStringListener {
        fun onNewFormattedLogString(logString: String) = run { }
    }
}