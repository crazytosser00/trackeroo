package ru.roansa.trackeroo_core.logging.file.text

import ru.roansa.trackeroo_core.logging.file.LogFileConfig
import java.io.File
import java.util.LinkedList

internal class LogFilesController(private val config: LogFileConfig) {
    /**
     * Counts every time after new log file created. Clears every new session (app restart)
     * It needs for avoiding log file name duplicates
     */
    private var index: Int = 1
    private val indexFormatter: String = "(%d)"
    private val regexpFileName: Regex = "^${config.logFileBaseName}[(]\\d+[)][.]?[a-z]*\$".toRegex()


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
        val (oldLogFiles, lastIndex) = findLogFilesWithIndex()
        if (oldLogFiles.isNotEmpty()) {
            logFiles.addAll(oldLogFiles)
            index = lastIndex
        }

        if (logFiles.isEmpty()) {
            logFiles.addFirst(createFile(false))
        }
    }

    private fun createFile(isNext: Boolean): File {
        with(config) {
            if (isNext) index++

            if (!logDirectory.exists()) logDirectory.mkdirs()
            val file = File(
                logDirectory,
                "$logFileBaseName${indexFormatter.format(index)}.$logFileExtension"
            )

            file.writeText("")
            return file
        }
    }

    private fun findLogFilesWithIndex(): Pair<List<File>, Int> {
        if (!logDirectory.exists()) return Pair(emptyList(), 0)

        val result = logDirectory.listFiles()?.let { files ->
            files
                .filter { it.name.matches(regexpFileName) }
                .mapNotNull { file ->
                    regexpFileName
                        .findAll(file.name, 0)
                        .map { it.groupValues[1] }.joinToString()
                        .let { index -> index.toIntOrNull()?.let { Pair(it, file) } }
                }
                .sortedByDescending { it.first }
        } ?: emptyList()

        val index = result.firstOrNull()?.first ?: return Pair(emptyList(), 0)
        return result.map { it.second } to index
    }

}