package ru.roansa.trackeroo_core.logging.file

class LogFileConfig(
    val filesDir: String,
    val logDirectoryName: String = "",
    val logFileBaseName: String = "",
    val logFileMaxSize: Long = 0,
    val logFilesMaxCount: Int = 0
) {

    companion object {
        fun empty(): LogFileConfig = LogFileConfig("")
    }

}