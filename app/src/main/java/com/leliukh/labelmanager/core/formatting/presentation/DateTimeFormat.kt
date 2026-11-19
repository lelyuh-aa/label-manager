package com.leliukh.labelmanager.core.formatting.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.ZoneId
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * A moment as the user reads it in a "data from …" notice: only the time for today, e.g.
 * "14:32", and the date with the time otherwise.
 *
 * @param instant the moment
 * @param now the current moment
 * @param zone the user's time zone
 * @param locale the user's language
 */
fun formatRecentMoment(
    instant: Instant,
    now: Instant = Clock.System.now(),
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String {
    val moment = instant.toJavaInstant().atZone(zone)
    val isToday = moment.toLocalDate() == now.toJavaInstant().atZone(zone).toLocalDate()
    val formatter = if (isToday) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    } else {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    }
    return formatter.withLocale(locale).format(moment)
}

/**
 * A calendar date in the user's medium style, e.g. "Oct 18, 2026" or "18 окт. 2026 г.".
 *
 * @param date the date
 * @param locale the user's language
 */
fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
        .format(date.toJavaLocalDate())

/**
 * A calendar date in digits with the full year, e.g. "09.06.2006" or "6/9/2006": the user's
 * short style, which keeps the order and separators of their language, with a four-digit year.
 *
 * @param date the date
 * @param locale the user's language
 */
fun formatNumericDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
    val shortPattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.SHORT,
        null,
        IsoChronology.INSTANCE,
        locale,
    )
    return DateTimeFormatter.ofPattern(shortPattern.replace(Regex("y+"), "yyyy"), locale)
        .format(date.toJavaLocalDate())
}

/**
 * The date of a moment in the user's time zone, in the medium style.
 *
 * @param instant the moment
 * @param zone the user's time zone
 * @param locale the user's language
 */
fun formatDate(
    instant: Instant,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault(),
): String = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    .withLocale(locale)
    .format(instant.toJavaInstant().atZone(zone))
