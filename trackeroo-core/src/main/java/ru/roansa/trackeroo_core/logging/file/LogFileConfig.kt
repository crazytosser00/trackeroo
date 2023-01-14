package ru.roansa.trackeroo_core.logging.file

import java.io.File

class LogFileConfig(
    private val filesDir: String,
    private val logDirectoryName: String = DEFAULT_DIRECTORY_NAME,
    private val logFileName: String = DEFAULT_FILE_NAME
) {
    val logDirectory: File = File("$filesDir${File.separator}$logDirectoryName")
    val logFile: File = File("$filesDir${File.separator}$logDirectoryName", logFileName)

    companion object {
        /**
         * This is the default path to directory where log file are stored
         * Full default path to directory is /data/user/{your.package.name}/files/trackeroo
         */
        const val DEFAULT_DIRECTORY_NAME = "trackeroo"

        /**
         * You should set file extension like .txt or .csv depending on what file type
         * requires current instance of ILogWriter
         */
        const val DEFAULT_FILE_NAME = "log.txt"
    }
}