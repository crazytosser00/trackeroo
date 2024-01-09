package ru.roansa.trackeroo_core.logging.file

import ru.roansa.trackeroo_core.logging.file.transform.ILogFileTransformer
import java.io.File

abstract class ILogFileWriter(private val logFileConfig: LogFileConfig) {
    abstract val transformer: ILogFileTransformer
    abstract val isTransformNeeded: Boolean
    abstract val logFile: File

    abstract fun write(string: String?)
    abstract fun clear()
}