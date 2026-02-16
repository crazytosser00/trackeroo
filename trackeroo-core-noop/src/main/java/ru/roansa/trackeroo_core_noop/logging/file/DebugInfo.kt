package ru.roansa.trackeroo_core.logging.file

/**
 * Диагностическая информация о состоянии логирования.
 *
 * @param emitCount количество вызовов метода write()
 * @param collectCount количество обработанных записей в collector
 * @param collectorActive флаг активности collector корутины
 * @param currentFilePath путь к текущему лог-файлу
 * @param currentFileSize размер текущего лог-файла в байтах
 * @param filesCount количество лог-файлов в директории
 * @param logDirectoryPath путь к директории с логами
 */
data class DebugInfo(
    val emitCount: Long = 0L,
    val collectCount: Long = 0L,
    val collectorActive: Boolean = false,
    val currentFilePath: String = "",
    val currentFileSize: Long = 0L,
    val filesCount: Int = 0,
    val logDirectoryPath: String = ""
)
