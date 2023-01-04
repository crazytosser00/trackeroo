package ru.roansa.trackeroo_core.logging.transform

import ru.roansa.trackeroo_core.logging.LogEntity
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

/**
 * @param locale - using for date formatting Locale.ENGLISH by default
 */
class TimeTransformer(locale: Locale = Locale.ENGLISH) : ILogTransformer {

    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SS", locale)

    override fun transform(previousResultString: String, logEntity: LogEntity) =
        "[${dateTimeFormat.format(Date(logEntity.timestamp))}] "

}