package ru.roansa.trackeroo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.roansa.trackeroo_core.logging.Logger
import ru.roansa.trackeroo_core.logging.file.DebugInfo
import java.io.File

/**
 * ViewModel для управления дебаггингом и тестированием логирования
 * @param debugInfo начальная debug-информация
 */
class DebugViewModel : ViewModel() {

    private var autoLoggingJob: Job? = null
    private val _status = MutableStateFlow<DebugStatus?>(null)
    val status: StateFlow<DebugStatus?> = _status.asStateFlow()

    private val _stressTestProgress = MutableStateFlow<StressTestProgress?>(null)
    val stressTestProgress: StateFlow<StressTestProgress?> = _stressTestProgress.asStateFlow()

    private val _logFiles = MutableStateFlow<List<LogFileItem>>(emptyList())
    val logFiles: StateFlow<List<LogFileItem>> = _logFiles.asStateFlow()

    init {
        refreshStatus()
        refreshLogFiles()
    }

    /**
     * Запускает автоматическое логирование с заданным интервалом
     * @param intervalMs интервал между записями в миллисекундах
     */
    fun startAutoLogging(intervalMs: Long) {
        stopAutoLogging()
        autoLoggingJob = viewModelScope.launch {
            var counter = 0L
            while (true) {
                counter++
                Logger.d("AutoLog", "Auto log entry #$counter at ${System.currentTimeMillis()}")
                delay(intervalMs)
                refreshStatus()
            }
        }
        refreshStatus()
    }

    /**
     * Останавливает автоматическое логирование
     */
    fun stopAutoLogging() {
        autoLoggingJob?.cancel()
        autoLoggingJob = null
        refreshStatus()
    }

    /**
     * Запускает стресс-тест с массовой записью логов
     * @param count количество записей для создания
     */
    fun runStressTest(count: Int) {
        viewModelScope.launch {
            _stressTestProgress.value = StressTestProgress(0, count, false)
            for (i in 1..count) {
                Logger.d("StressTest", "Stress test entry #$i of $count")
                if (i % 100 == 0) {
                    _stressTestProgress.value = StressTestProgress(i, count, false)
                    delay(10)
                }
            }
            _stressTestProgress.value = StressTestProgress(count, count, true)
            refreshStatus()
            refreshLogFiles()
            delay(2000)
            _stressTestProgress.value = null
        }
    }

    /**
     * Обновляет статус логирования из библиотеки
     */
    fun refreshStatus() {
        val debugInfo = Logger.getDebugStatus()
        _status.value = debugInfo?.let {
            DebugStatus(
                emitCount = it.emitCount,
                collectCount = it.collectCount,
                collectorActive = it.collectorActive,
                currentFile = it.currentFilePath,
                currentFileSize = it.currentFileSize,
                filesCount = it.filesCount,
                autoLoggingActive = autoLoggingJob?.isActive == true
            )
        }
    }

    /**
     * Обновляет список лог-файлов
     */
    fun refreshLogFiles() {
        val debugInfo = Logger.getDebugStatus() ?: return
        val logDir = File(debugInfo.logDirectoryPath)
        if (!logDir.exists()) {
            _logFiles.value = emptyList()
            return
        }

        val files = logDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                LogFileItem(
                    name = file.name,
                    path = file.path,
                    size = file.length(),
                    lastModified = file.lastModified()
                )
            } ?: emptyList()

        _logFiles.value = files
    }

    /**
     * Читает содержимое файла
     * @param filePath путь к файлу
     * @return содержимое файла или null если файл не существует
     */
    fun readFileContent(filePath: String): String? {
        return try {
            File(filePath).readText()
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoLogging()
    }
}

/**
 * Статус логирования для отображения в UI
 */
data class DebugStatus(
    val emitCount: Long,
    val collectCount: Long,
    val collectorActive: Boolean,
    val currentFile: String,
    val currentFileSize: Long,
    val filesCount: Int,
    val autoLoggingActive: Boolean
)

/**
 * Прогресс выполнения стресс-теста
 */
data class StressTestProgress(
    val current: Int,
    val total: Int,
    val completed: Boolean
)

/**
 * Информация о лог-файле
 */
data class LogFileItem(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)
