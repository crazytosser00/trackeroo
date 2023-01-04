package ru.roansa.trackeroo_core.logging

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File

class LogFileWriter(
    pathToFile: String,
    fileName: String,
    context: Context,
    private val printOnNewLine: Boolean = true
) : ILogWriter {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val logFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 100
    )

    private val directory = File("${context.filesDir}${File.separator}$pathToFile")
    override val logFile = File("${context.filesDir}${File.separator}$pathToFile", fileName)

    init {
        coroutineScope.launch {
            if (!directory.exists()) directory.mkdirs()

            logFlow.collect { str ->
                logFile.appendText(str)
                if (printOnNewLine) logFile.appendText("\n")
            }
        }
    }

    override fun write(string: String) {
        coroutineScope.launch { logFlow.emit(string) }
    }

    override fun clear() {
        logFile.writeText("")
    }

}