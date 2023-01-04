package ru.roansa.trackeroo_core.publish

import java.io.File

interface ILogPublisher {

    fun publish(file: File)

}