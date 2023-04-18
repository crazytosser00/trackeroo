package ru.roansa.trackeroo_core.logging.file

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.roansa.trackeroo_core.ext.doIfExist
import java.io.File

class LogTextFileWriter(
    override val logFileConfig: LogFileConfig?,
    private val printOnNewLine: Boolean = true
) : ILogWriter {

    private val logFile: File? get() = logFileConfig?.logFile
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val logFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 100
    )

    init {
        if (logFile?.exists() != null) logFile?.createNewFile()

        coroutineScope.launch {
            logFlow.collect { str ->
                if (logFileConfig?.logDirectory?.exists() != true) logFileConfig?.logDirectory?.mkdirs()

                logFile.doIfExist {
                    appendText(str)
                    if (printOnNewLine) appendText("\n")
                }
            }
        }
    }

    override fun write(string: String) {
        coroutineScope.launch { logFlow.emit(string) }
    }

    override fun clear() {
        logFile.doIfExist { writeText("") }
    }

    private fun createFileIfNotExist() {
        logFile?.createNewFile()
    }

}