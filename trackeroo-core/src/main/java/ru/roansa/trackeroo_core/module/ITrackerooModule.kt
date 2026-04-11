package ru.roansa.trackeroo_core.module

/**
 * Интерфейс модуля SDK Trackeroo.
 *
 * Модули подключаются к логгеру через {@link ru.roansa.trackeroo_core.logging.Logger.Builder#addModule}
 * и управляют своим жизненным циклом через методы start/stop.
 */
interface ITrackerooModule {

    /**
     * Запускает модуль. Вызывается автоматически при {@code Logger.Builder.build()},
     * а также может быть вызван вручную для повторного запуска.
     */
    fun start()

    /**
     * Останавливает модуль и освобождает ресурсы.
     */
    fun stop()

    /**
     * Проверяет, запущен ли модуль.
     *
     * @return true если модуль активен
     */
    fun isRunning(): Boolean
}
