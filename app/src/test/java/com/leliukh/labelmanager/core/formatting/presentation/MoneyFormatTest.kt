package com.leliukh.labelmanager.core.formatting.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun `full format groups thousands with commas`() {
        assertEquals("$3,482,150", formatMoney(3_482_150))
        assertEquals("$0", formatMoney(0))
        assertEquals("-$1,450,000", formatMoney(-1_450_000))
    }

    @Test
    fun `compact format keeps up to two decimals and drops trailing zeros`() {
        assertEquals("$940K", formatMoneyCompact(940_000))
        assertEquals("$1M", formatMoneyCompact(1_000_000))
        assertEquals("$1.45M", formatMoneyCompact(1_450_000))
        assertEquals("$23.64M", formatMoneyCompact(23_640_000))
        assertEquals("$1.2B", formatMoneyCompact(1_200_000_000))
        assertEquals("$999", formatMoneyCompact(999))
    }

    @Test
    fun `percent change is rounded to a whole percent`() {
        assertEquals("30%", formatPercentChange(1_000_000, 1_300_000))
        assertEquals("100%", formatPercentChange(1_000_000, 2_000_000))
        assertEquals("0%", formatPercentChange(0, 5))
    }

    @Test
    fun `flag emoji is built from regional indicators`() {
        assertEquals("🇩🇪", countryFlagEmoji("de"))
        assertEquals("", countryFlagEmoji("DEU"))
    }
}
