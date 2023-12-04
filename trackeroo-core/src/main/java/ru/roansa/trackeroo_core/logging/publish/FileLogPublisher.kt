package ru.roansa.trackeroo_core.logging.publish

import java.io.File

abstract class FileLogPublisher: ILogPublisher<File>() {

    override fun prepare(target: File): File = target

}