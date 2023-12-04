package ru.roansa.trackeroo_core.logging.file.transform

import java.io.File

interface ILogFileTransformer {

    fun transform(target: File): File

}