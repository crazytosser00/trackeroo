package ru.roansa.trackeroo_core.logging

class LogEntity(
    val level: LogLevel = LogLevel.UNDEFINED,
    val tag: String? = null,
    val message: String? = null,
    val throwable: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogLevel {
    UNDEFINED, VERBOSE, DEBUG, INFO, WARNING, ERROR, ASSERT
}