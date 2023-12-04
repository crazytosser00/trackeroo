package ru.roansa.trackeroo_core.logging.file.text

import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import java.io.File
import java.util.LinkedList

internal class LogFilesController(private val config: LogFileConfig) {

    /**
     * Counts every time when new log file created. Clears every new session (app restart)
     * It needs for avoiding log file name duplicates
     */
    private var index: Int = 1

    /** Optimized collection for LIFO */
    private val logFiles: LinkedList<File> = LinkedList()

    internal val logDirectory: File =
        with(config) { File("$filesDir${File.separator}$logDirectoryName") }
    internal val currentLogFile: File get() = logFiles.first

    init {
        init()
    }

    internal fun updateLogFiles() {
        config.run {
            if (currentLogFile.length() > logFileMaxSize) {
                val filesCount = logFiles.size
                if (filesCount == logFilesMaxCount) {
                    logFiles.lastOrNull()?.delete()
                    logFiles.removeLast()
                }

                val newFile = createFile(true)
                logFiles.addFirst(newFile)
            }
        }
    }

    internal fun clear() {
        logDirectory.listFiles()?.forEach { it.delete() }
        logFiles.clear()
        init()
    }

    internal fun init() {
        if (logFiles.isEmpty()) {
            logFiles.addFirst(createFile(false))
        }
    }

    private fun createFile(isNext: Boolean): File {
        with(config) {
            if (isNext) index++

            if (!logDirectory.exists())  logDirectory.mkdirs()
            val file = File(logDirectory, logFileBaseName.format(index))

            file.writeText("")
            return file
        }
    }

}