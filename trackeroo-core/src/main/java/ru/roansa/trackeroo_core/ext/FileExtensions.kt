package ru.roansa.trackeroo_core.ext

import java.io.File

internal inline fun File?.doIfExist(block: File.() -> Unit) {
    if (this != null && isFile) { block(this) }
}