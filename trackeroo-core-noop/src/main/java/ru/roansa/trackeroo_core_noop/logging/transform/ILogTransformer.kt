package ru.roansa.trackeroo_core.logging.transform

import ru.roansa.trackeroo_core.logging.LogEntity

interface ILogTransformer {

    fun shouldTransform(logEntity: LogEntity): Boolean = true

    /**
     * This method transforms log data to string formatted by given rules.
     * You can makes all changes in one class but I strongly recommend divide transformation.
     * One transform in one class.
     */
    fun transform(previousResultString: String, logEntity: LogEntity): String

}