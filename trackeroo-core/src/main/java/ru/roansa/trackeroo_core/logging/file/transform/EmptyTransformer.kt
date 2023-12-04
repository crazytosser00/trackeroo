package ru.roansa.trackeroo_core.logging.file.transform

import java.io.File

class EmptyTransformer : ILogFileTransformer {

    override fun transform(target: File): File = target

}