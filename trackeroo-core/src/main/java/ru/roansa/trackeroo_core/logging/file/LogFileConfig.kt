package ru.roansa.trackeroo_core.logging.file

/**
 * @param filesDir app data directory @see android.content.Context.getFilesDir()
 * @param logDirectoryName name of a folder where log files would be saved
 * @param logFileBaseName base name of a file. All log files will be named with next pattern: "${logFileBaseName}_${countNumber}". For example log_1
 * @param logFileMaxSize max value in bytes for every log file. Default is 5 Mb, max recommended is 20 Mb
 * @param logFilesMaxCount max value of log files count. Default is 5
 */
class LogFileConfig(
    val filesDir: String,
    val logDirectoryName: String = DEFAULT_DIRECTORY_NAME,
    val logFileBaseName: String = DEFAULT_FILE_NAME,
    val logFileMaxSize: Long = DEFAULT_FILE_SIZE,
    val logFilesMaxCount: Int = DEFAULT_LOG_FILES_MAX_COUNT
) {
    companion object {

        fun empty(): LogFileConfig = LogFileConfig("")

        /**
         * This is the default path to directory where log file are stored
         * Full default path to directory is /data/user/{your.package.name}/files/trackeroo
         * Or /data/data/{your.package.name}/files/trackeroo
         */
        const val DEFAULT_DIRECTORY_NAME = "trackeroo"

        /**
         * You should set file extension like .txt or .csv depending on
         * what file type requires current instance of ILogWriter
         */
        const val DEFAULT_FILE_NAME = "log_%d.txt"


        const val DEFAULT_FILE_SIZE = 5242880L

        const val DEFAULT_LOG_FILES_MAX_COUNT = 5
    }
}