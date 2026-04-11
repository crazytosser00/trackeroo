package ru.roansa.trackeroo_core.logging.file.text

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.roansa.trackeroo_core.logging.file.DebugInfo
import ru.roansa.trackeroo_core.logging.file.ILogFileWriter
import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import ru.roansa.trackeroo_core.logging.file.transform.ILogFileTransformer
import ru.roansa.trackeroo_core.logging.file.transform.ZipTransformer
import java.io.File
import java.io.IOException

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
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 10000
    )

    @Volatile private var collectorActive = false
    @Volatile private var emitCount = 0L
    @Volatile private var collectCount = 0L

    init {
        if (!logDirectory.exists()) logDirectory.mkdirs()
        if (!currentLogFile.exists()) logFilesController.init()

        startCollector()
    }

    /**
     * Запускает collector с автоматическим перезапуском при падении
     */
    private fun startCollector() {
        coroutineScope.launch {
            while (isActive) {
                try {
                    collectorActive = true
                    logFlow.collect { str ->
                        collectCount++
                        writeToFile(str)
                    }
                } catch (e: CancellationException) {
                    collectorActive = false
                    throw e  // не глотаем отмену
                } catch (e: Exception) {
                    collectorActive = false
                    delay(1000)  // пауза перед перезапуском
                }
            }
            collectorActive = false
        }
    }

    /**
     * Безопасная запись в файл с обработкой исключений и восстановлением файла при необходимости
     * @param str строка для записи
     */
    private fun writeToFile(str: String) {
        try {
            val file = currentLogFile
            if (file.exists() && file.isFile) {
                file.appendText(str)
                if (printOnNewLine) file.appendText("\n")
            } else {
                logFilesController.init()  // восстановление файла
                val newFile = currentLogFile
                if (newFile.exists() && newFile.isFile) {
                    newFile.appendText(str)
                    if (printOnNewLine) newFile.appendText("\n")
                }
            }
        } catch (e: IOException) {
            // логируем в logcat, но не падаем
            android.util.Log.w("Trackeroo-LogWriter", "Failed to write log to file", e)
        }
    }

    override fun clear() {
        logFilesController.clear()
    }

    /**
     * Корректно завершает работу writer'а, отменяя все корутины
     */
    fun close() {
        coroutineScope.cancel()
    }

    override fun getDebugInfo(): DebugInfo {
        val currentFile = currentLogFile
        val logDir = logDirectory
        val filesCount = logDir.listFiles()?.size ?: 0
        
        return DebugInfo(
            emitCount = emitCount,
            collectCount = collectCount,
            collectorActive = collectorActive,
            currentFilePath = currentFile.path,
            currentFileSize = if (currentFile.exists()) currentFile.length() else 0L,
            filesCount = filesCount,
            logDirectoryPath = logDir.path
        )
    }

    override fun write(string: String?) {
        if (string == null) return

        emitCount++
        logFilesController.updateLogFiles()
        coroutineScope.launch { logFlow.emit(string) }
    }
}