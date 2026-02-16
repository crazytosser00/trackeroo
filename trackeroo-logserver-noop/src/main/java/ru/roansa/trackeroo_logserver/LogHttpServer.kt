package ru.roansa.trackeroo_logserver

import android.content.Context
import android.util.Log
import ru.roansa.trackeroo_core.module.ITrackerooModule

/**
 * No-op реализация LogHttpServer для release-сборок.
 * Не наследует NanoHTTPD и не имеет зависимости от него.
 *
 * @param context контекст приложения
 * @param port порт сервера
 */
class LogHttpServer(
    private val context: Context,
    port: Int = 8888
) : ITrackerooModule {

    companion object {
        private const val TAG = "LogHttpServer"

        /**
         * Безопасный запуск сервера. В no-op версии всегда возвращает null.
         *
         * @param context контекст приложения
         * @param port порт сервера (игнорируется)
         * @return всегда null
         */
        @Deprecated(
            "Use ITrackerooModule.start() instead",
            ReplaceWith("LogHttpServer(context, port).also { it.start() }")
        )
        fun startSafely(context: Context, port: Int = 8888): LogHttpServer? {
            Log.d(TAG, "LogHttpServer is disabled in no-op build")
            return null
        }
    }

    /**
     * Запускает модуль. В no-op версии ничего не делает.
     */
    override fun start() {
        // No-op
    }

    /**
     * Останавливает модуль. В no-op версии ничего не делает.
     */
    override fun stop() {
        // No-op
    }

    /**
     * Проверяет, запущен ли модуль. В no-op версии всегда false.
     *
     * @return всегда false
     */
    override fun isRunning(): Boolean = false

    /**
     * Возвращает URL запущенного сервера. В no-op версии всегда null.
     *
     * @return всегда null
     */
    fun getServerUrl(): String? = null

    /**
     * Безопасная остановка сервера. В no-op версии ничего не делает.
     */
    @Deprecated(
        "Use ITrackerooModule.stop() instead",
        ReplaceWith("stop()")
    )
    fun stopSafely() {
        // No-op
    }
}
