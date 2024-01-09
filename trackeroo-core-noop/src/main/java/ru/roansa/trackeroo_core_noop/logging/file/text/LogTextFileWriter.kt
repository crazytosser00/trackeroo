package ru.roansa.trackeroo_core.logging.file.text

import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import ru.roansa.trackeroo_core.logging.file.transform.ILogFileTransformer
import java.io.File

class LogTextFileWriter(
    logFileConfig: LogFileConfig,
    private val printOnNewLine: Boolean = true
) : ILogFileWriter(logFileConfig) {

    override val transformer: ILogFileTransformer get() = NoOpTransformer()
    override val isTransformNeeded: Boolean get() = false
    override val logFile: File get() = File("")

    override fun clear() {}

    override fun write(string: String?) {}

    private class NoOpTransformer : ILogFileTransformer {
        override fun transform(target: File): File = target
    }
}