package ru.roansa.trackeroo_core.logging.transform

import ru.roansa.trackeroo_core.logging.LogEntity

/**
 * This class generates a string with LogEntity transformed data
 * All transformers applies in order they were added to LogFormatter
 *
 * I.e. you add time transformer first. That transformer gives the following result - "[01.01.2022 18:00] "
 * Next you add debug level transformer. It gives result - "DEBUG: "
 *
 * So your transformed string will look like that - "[01.01.2022 18:00] DEBUG: "
 * Swapping transformers order gives you another string - "DEBUG: [01.01.2022 18:00]"
 *
 * @see LogEntity
 * @see ILogTransformer
 */
open class LogFormatter {

    private val transformers: MutableList<ILogTransformer> = mutableListOf()

    fun applyTransformer(transformer: ILogTransformer) = apply {
        transformers.add(transformer)
    }

    fun isEmpty() = transformers.isEmpty()

    open fun transform(logEntity: LogEntity): String {
        var transformedString = ""
        transformers.forEach { transformer ->
            transformedString += transformer.transform(transformedString, logEntity)
        }
        return transformedString
    }

}