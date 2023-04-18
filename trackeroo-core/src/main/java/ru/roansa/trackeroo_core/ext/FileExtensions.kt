package ru.roansa.trackeroo_core.ext

import java.io.File

// Need to create function that give possibility to call method without "it" keyword
inline fun File?.doIfExist(block: File.() -> Unit) {
    if (this != null && isFile) { block(this) }
}