package ru.roansa.trackeroo_core.logging.publish

import java.io.File

interface ILogPublisher {

    fun publish(file: File)

}