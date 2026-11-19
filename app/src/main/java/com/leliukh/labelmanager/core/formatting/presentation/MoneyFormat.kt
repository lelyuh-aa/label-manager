package com.leliukh.labelmanager.core.formatting.presentation

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private const val THOUSAND = 1_000L
private const val MILLION = 1_000_000L
private const val BILLION = 1_000_000_000L
private const val MASK = "•••••••"

/**
 * Formats whole US dollars in full, e.g. `$3,482,150`.
 *
 * Money is always US dollars in this game, so the US format is used whatever the device
 * locale is.
 *
 * @param amount amount in whole dollars
 */
fun formatMoney(amount: Long): String {
    val digits = NumberFormat.getIntegerInstance(Locale.US).format(abs(amount))
    return if (amount < 0) "-$$digits" else "$$digits"
}

/**
 * Formats whole US dollars compactly for dense lists: `$940K`, `$1.45M`, `$23.64M`.
 *
 * @param amount amount in whole dollars
 */
fun formatMoneyCompact(amount: Long): String {
    val absolute = abs(amount)
    val sign = if (amount < 0) "-" else ""
    val body = when {
        absolute >= BILLION -> scaled(absolute, BILLION) + "B"

        absolute >= MILLION -> scaled(absolute, MILLION) + "M"

        absolute >= THOUSAND -> scaled(absolute, THOUSAND) + "K"

        else -> absolute.toString()
    }
    return "$sign$$body"
}

/**
 * The placeholder shown instead of an amount while the budget is hidden.
 */
fun maskedMoney(): String = "$$MASK"

/**
 * Formats the relative change between two amounts as a whole percentage, e.g. `38%`.
 *
 * @param before the earlier amount; zero yields `0%`
 * @param after the later amount
 */
fun formatPercentChange(before: Long, after: Long): String {
    if (before == 0L) return "0%"
    val percent = BigDecimal(abs(after - before) * 100)
        .divide(BigDecimal(before), 0, RoundingMode.HALF_UP)
    return "$percent%"
}

private fun scaled(value: Long, unit: Long): String =
    BigDecimal(value)
        .divide(BigDecimal(unit), 2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
