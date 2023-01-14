package ru.roansa.trackeroo_core.logging.file

import java.io.File

interface ILogWriter {

    val logFileConfig: LogFileConfig?

    fun clear()
    //TODO make string nullable
    fun write(string: String)

}