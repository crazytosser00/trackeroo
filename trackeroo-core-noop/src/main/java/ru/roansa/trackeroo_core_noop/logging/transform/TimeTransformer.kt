package ru.roansa.trackeroo_core.logging.transform

import ru.roansa.trackeroo_core.logging.LogEntity
import java.util.Locale

/**
 * @param locale - using for date formatting Locale.ENGLISH by default
 */
class TimeTransformer(locale: Locale = Locale.ENGLISH) : ILogTransformer {
    override fun transform(previousResultString: String, logEntity: LogEntity) = ""
}