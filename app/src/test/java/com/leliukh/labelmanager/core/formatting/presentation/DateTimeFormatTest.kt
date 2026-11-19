package com.leliukh.labelmanager.core.formatting.presentation

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class DateTimeFormatTest {

    private val date = LocalDate(2006, 6, 9)

    @Test
    fun `a numeric date keeps the order of the language and the full year`() {
        assertEquals("09.06.2006", formatNumericDate(date, Locale.forLanguageTag("ru-RU")))
        assertEquals("6/9/2006", formatNumericDate(date, Locale.US))
    }
}
