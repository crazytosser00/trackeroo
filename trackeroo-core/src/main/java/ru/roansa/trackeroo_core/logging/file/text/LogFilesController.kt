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
    private val regexpIndex: Regex = "(\\d+)".toRegex()


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
                deleteOldestLogFile()
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

            /**
             * This code will delete oldest log files in case if there is more than max count of it
             * In normal working mode this situation can't exist
             * But for any case (i just may made a bug anyway) this code will be added
             */
            while (config.deleteOldestLogFile()) {}
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
                    regexpIndex
                        .findAll(file.name, 0)
                        .map { it.groupValues.getOrNull(1) ?: return@map null }.joinToString()
                        .let { index -> index.toIntOrNull()?.let { Pair(it, file) } }
                }
                .sortedByDescending { it.first }
        } ?: emptyList()

        val index = result.firstOrNull()?.first ?: return Pair(emptyList(), 0)
        return result.map { it.second } to index
    }

    /**
     * Method returns true if max number of log files has been exceeded and oldest file has been deleted
     * Returns false if no files has been removed
     */
    private fun LogFileConfig.deleteOldestLogFile(): Boolean {
        val filesCount = logFiles.size
        return if (filesCount >= logFilesMaxCount) {
            logFiles.lastOrNull()?.delete()
            logFiles.removeLast()
            true
        } else false
    }
}