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
}