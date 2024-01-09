package ru.roansa.trackeroo_core.logging.publish

import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import java.io.File
import java.io.InvalidObjectException

abstract class ILogPublisher<T> {

    /**
     * Method generates an action to publish (upload, share, etc.) a file with log data
     * Before this action method makes any transformations declared in ILogFileWriter.transformer
     */
    @Throws(InvalidObjectException::class)
    fun publish() {}

    @Throws(InvalidObjectException::class)
    protected abstract fun publish(target: T)

    protected abstract fun prepare(target: File): T

    internal fun setLogWriter(logWriter: ILogFileWriter) {}

}