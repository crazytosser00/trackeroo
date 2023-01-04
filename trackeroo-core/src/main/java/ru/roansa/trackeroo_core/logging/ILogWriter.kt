package ru.roansa.trackeroo_core.logging

import java.io.File

interface ILogWriter {

    val logFile: File

    fun clear()
    //TODO make string nullable
    fun write(string: String)

}