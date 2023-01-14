package ru.roansa.trackeroo_core.logging.publish

import java.io.File

interface ILogPublisher<T> {

    fun publish(file: File): T

}