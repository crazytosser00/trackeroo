package ru.roansa.trackeroo_core

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun regexExtraction_isCorrect() {
        val regexpFindIndex = "^log[(](\\d+)[)][.]?[a-z]*\$".toRegex()
        val testFileName = "log(1).txt"
        val matchResult = regexpFindIndex.find(testFileName, 0)?.groupValues?.get(1) ?: ""
        assertEquals(matchResult, "1")
    }

    @Test
    fun findLogFilesIndex_isCorrect() {
        val logFileBaseName = "log"
        val files = arrayListOf(
            "log(13).txt",
            "log(18).txt",
            "log(22).txt",
            "log(55).txt",
            "log(199).txt",
            "log(140).txt",
            "log(341).txt",
            "log(64).txt"
        )
        val regexpFileName: Regex = "^${logFileBaseName}[(]\\d+[)][.]?[a-z]*\$".toRegex()
        val regexpIndex: Regex = "(\\d+)".toRegex()
        val result = files
            .filter { it.matches(regexpFileName) }
            .mapNotNull { file ->
                regexpIndex
                    .findAll(file, 0)
                    .map { it.groupValues.getOrNull(1) ?: return@map null }.joinToString()
                    .let { index -> index.toIntOrNull()?.let { Pair(it, file) } }
            }
            .sortedByDescending { it.first }
        assertEquals(341, result.first().first)
    }
}