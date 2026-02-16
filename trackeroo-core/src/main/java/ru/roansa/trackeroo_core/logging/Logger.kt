package ru.roansa.trackeroo_core.logging

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import ru.roansa.trackeroo_core.hookers.ViewHooker
import ru.roansa.trackeroo_core.hookers.exception.DefaultUncaughtExceptionHooker
import ru.roansa.trackeroo_core.hookers.exception.UncaughtExceptionAction
import ru.roansa.trackeroo_core.logging.file.DebugInfo
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
import ru.roansa.trackeroo_core.module.ITrackerooModule
import java.util.concurrent.CopyOnWriteArrayList

//TODO add exceptions to necessary fields when it is not initialized
//TODO add methods like default logging methods (like w(), d() etc.) but with LogEntity parameters variable as method parameter
object Logger {

    const val TAG: String = "Trackeroo-Logger"
    private var logWriter: ILogFileWriter? = null
    private var logFormatter: LogFormatter? = null
    private var logPublisher: ILogPublisher<*>? = null
    private val logStringListeners = CopyOnWriteArrayList<OnNewFormattedLogStringListener>()
    private val modules = mutableListOf<ITrackerooModule>()
    var logFileConfig: LogFileConfig = LogFileConfig.empty()
        private set

    /**
     * It's better to call this method in Application.attachBaseContext
     * or in Application.onCreate before super.onCreate
     *
     * This will maximize chances that only one application Thread will be create at this moment.
     * In other cases library cannot guarantee full log and error interceptions
     */
    fun builder(
        context: Context,
        logDirectoryName: String = LogFileConfig.DEFAULT_DIRECTORY_NAME,
        logFileName: String = LogFileConfig.DEFAULT_FILE_NAME
    ): Builder {
        val builder = Builder(context, logDirectoryName, logFileName)
        logFileConfig = builder.logFileConfig
        return builder
    }

    /**
     * It's better to call this method in Application.attachBaseContext
     * or in Application.onCreate before super.onCreate
     *
     * This will maximize chances that only one application Thread will be create at this moment.
     * In other cases library cannot guarantee full log and error interceptions
     */
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
        this@Logger.modules.addAll(this.modules)
        this@Logger.modules.forEach { it.start() }
    }

    /**
     * Call this method whenever library must export all accumulated logs
     */
    fun publish(): Any? =
        logFileConfig.run {
            logPublisher?.publish()
        }

    /**
     * Call this method whenever library must delete all accumulated logs
     */
    fun clearLogs() {
        logWriter?.clear()
    }

    /**
     * Возвращает диагностическую информацию о состоянии логирования
     * @return DebugInfo с информацией о состоянии, или null если диагностика недоступна
     */
    fun getDebugStatus(): DebugInfo? = logWriter?.getDebugInfo()

    /**
     * Добавляет слушателя новых отформатированных строк лога.
     *
     * @param listener слушатель для добавления
     */
    fun addOnNewFormattedLogStringListener(listener: OnNewFormattedLogStringListener) {
        logStringListeners.add(listener)
    }

    /**
     * Удаляет ранее добавленного слушателя.
     *
     * @param listener слушатель для удаления
     */
    fun removeOnNewFormattedLogStringListener(listener: OnNewFormattedLogStringListener) {
        logStringListeners.remove(listener)
    }

    /**
     * Останавливает все зарегистрированные модули.
     */
    fun stopAllModules() {
        modules.forEach { it.stop() }
    }

    @Deprecated(
        "Use addOnNewFormattedLogStringListener instead",
        ReplaceWith("addOnNewFormattedLogStringListener(listener)")
    )
    fun setOnNewFormattedLogStringListener(block: (String) -> Unit) {
        logStringListeners.clear()
        addOnNewFormattedLogStringListener(object : OnNewFormattedLogStringListener {
            override fun onNewFormattedLogString(logString: String) {
                block(logString)
            }
        })
    }

    @Deprecated(
        "Use addOnNewFormattedLogStringListener instead",
        ReplaceWith("addOnNewFormattedLogStringListener(onNewLogStringListener)")
    )
    fun setOnNewFormattedLogStringListener(onNewLogStringListener: OnNewFormattedLogStringListener) {
        logStringListeners.clear()
        addOnNewFormattedLogStringListener(onNewLogStringListener)
    }

    @JvmStatic
    fun v(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.VERBOSE, tag, message))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.v(tag, message)
    }

    @JvmStatic
    fun v(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString =
            logFormatter?.transform(LogEntity(LogLevel.VERBOSE, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.v(tag, message, throwable)
    }

    @JvmStatic
    fun d(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.DEBUG, tag, message))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.d(tag, message)
    }

    @JvmStatic
    fun d(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.DEBUG, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.d(tag, message, throwable)
    }

    @JvmStatic
    fun i(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.INFO, tag, message))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.i(tag, message)
    }

    @JvmStatic
    fun i(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.INFO, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.i(tag, message, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, message))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.w(tag, message)
    }

    @JvmStatic
    fun w(tag: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, null, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.w(tag, throwable)
    }

    @JvmStatic
    fun w(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString =
            logFormatter?.transform(LogEntity(LogLevel.WARNING, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun e(tag: String?, message: String): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ERROR, tag, message))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ERROR, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.e(tag, message, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.wtf(tag, message)
    }

    @JvmStatic
    fun wtf(tag: String?, throwable: Throwable): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, null, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.wtf(tag, throwable)
    }

    @JvmStatic
    fun wtf(tag: String?, message: String?, throwable: Throwable?): Int {
        val logString = logFormatter?.transform(LogEntity(LogLevel.ASSERT, tag, message, throwable))
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
        return Log.w(tag, message, throwable)
    }

    @JvmStatic
    fun log(logEntity: LogEntity): Int {
        val logString = logFormatter?.transform(logEntity)
        logWriter?.write(logString)
        logString?.let { notifyListeners(it) }
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
        logString?.let { notifyListeners(it) }
        return Log.println(priority, tag, message.orEmpty())
    }

    /**
     * Рассылает отформатированную строку лога всем зарегистрированным слушателям.
     *
     * @param logString отформатированная строка лога
     */
    private fun notifyListeners(logString: String) {
        for (listener in logStringListeners) {
            listener.onNewFormattedLogString(logString)
        }
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
            DefaultUncaughtExceptionHooker(
                applicationContext,
                UncaughtExceptionAction.CustomAction { _, _ -> })

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

        fun setUncaughtExceptionHooker(uncaughtExceptionHooker: DefaultUncaughtExceptionHooker): Builder {
            this.uncaughtExceptionHooker = uncaughtExceptionHooker
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHooker)
            return this
        }

        fun addLogTransformer(logTransformer: ILogTransformer): Builder {
            logFormatter.applyTransformer(logTransformer)
            return this
        }

        internal val modules: MutableList<ITrackerooModule> = mutableListOf()

        /**
         * Добавляет модуль, который будет запущен при вызове build().
         *
         * @param module модуль для добавления
         * @return текущий Builder
         */
        fun addModule(module: ITrackerooModule): Builder {
            modules.add(module)
            return this
        }
    }

    interface OnNewFormattedLogStringListener {
        fun onNewFormattedLogString(logString: String)
    }
}