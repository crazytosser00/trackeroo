package ru.roansa.trackeroo_core.logging.publish

import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import java.io.File
import java.io.InvalidObjectException

abstract class ILogPublisher<T> {

    private var logWriter: ILogFileWriter? = null

    /**
     * Method generates an action to publish (upload, share, etc.) a file with log data
     * Before this action method makes any transformations declared in ILogFileWriter.transformer
     */
    @Throws(InvalidObjectException::class)
    fun publish() {
        publish(beforePublish())
    }

    @Throws(InvalidObjectException::class)
    protected abstract fun publish(target: T)

    protected abstract fun prepare(target: File): T

    internal fun setLogWriter(logWriter: ILogFileWriter) {
        this.logWriter = logWriter
    }

    private fun beforePublish(): T {
        logWriter?.let { writer ->
            if (writer.isTransformNeeded) {
                val newTarget = writer.transformer.transform(writer.logFile)
                return prepare(newTarget)
            } else {
                return prepare(writer.logFile)
            }
        } ?: throw InvalidObjectException("ILogFileWriter object is not initialized")
    }
}

interface ILogPublisherListener