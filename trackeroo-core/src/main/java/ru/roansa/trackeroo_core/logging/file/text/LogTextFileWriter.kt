package ru.roansa.trackeroo_core.logging.file.text

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.roansa.trackeroo_core.ext.doIfExist
import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import ru.roansa.trackeroo_core.logging.file.transform.ILogFileTransformer
import ru.roansa.trackeroo_core.logging.file.transform.ZipTransformer
import java.io.File

class LogTextFileWriter(
    logFileConfig: LogFileConfig,
    private val printOnNewLine: Boolean = true
) : ILogFileWriter(logFileConfig) {

    override val transformer: ILogFileTransformer get() = ZipTransformer()
    override val isTransformNeeded: Boolean get() = true
    override val logFile: File get() = logDirectory

    private val logFilesController = LogFilesController(logFileConfig)
    private val logDirectory: File get() = logFilesController.logDirectory
    private val currentLogFile: File get() = logFilesController.currentLogFile
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val logFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 10000
    )

    init {
        if (!logDirectory.exists()) logDirectory.mkdirs()
        if (!currentLogFile.exists()) logFilesController.init()

        coroutineScope.launch {
            runBlocking {
                logFlow.collect { str ->
                    currentLogFile.doIfExist {
                        appendText(str)
                        if (printOnNewLine) appendText("\n")
                    }
                }
            }
        }
    }

    override fun clear() {
        logFilesController.clear()
    }

    override fun write(string: String?) {
        if (string == null) return

        currentLogFile.doIfExist {
            logFilesController.updateLogFiles()
            coroutineScope.launch { logFlow.emit(string) }
        }
    }
}