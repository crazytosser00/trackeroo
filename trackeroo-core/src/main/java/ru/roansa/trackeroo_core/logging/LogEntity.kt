package ru.roansa.trackeroo_core.logging

class LogEntity(
    val level: LogLevel = LogLevel.UNDEFINED,
    val tag: String? = null,
    val message: String? = null,
    val throwable: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val parameters: HashMap<String, Any>? = null
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> extractParam(key: String): T? {
        return try {
            parameters?.get(key) as? T
        } catch (e: ClassCastException) {
            null
        }
    }
}

enum class LogLevel {
    UNDEFINED, VERBOSE, DEBUG, INFO, WARNING, ERROR, ASSERT
}