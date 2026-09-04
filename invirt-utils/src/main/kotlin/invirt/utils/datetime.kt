package invirt.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.toJavaDuration

fun Instant.plusDays(days: Int): Instant = this.plus(days.toLong(), ChronoUnit.DAYS)

fun Instant.minusDays(days: Int): Instant = this.minus(days.toLong(), ChronoUnit.DAYS)

/**
 * The English ordinal suffix ("st", "nd", "rd", "th") for this date's day of month.
 */
fun LocalDate.dayOfMonthSuffix(): String = dayOfMonth.dayOfMonthSuffix()

/**
 * The English ordinal suffix ("st", "nd", "rd", "th") for this date-time's day of month.
 */
fun LocalDateTime.dayOfMonthSuffix(): String = dayOfMonth.dayOfMonthSuffix()

/**
 * The English ordinal suffix ("st", "nd", "rd", "th") for the day of month this instant falls on in [zone].
 *
 * An instant is a point on the timeline and has no calendar day of its own - the same instant is the 31st
 * in one zone and the 1st in another - so [zone] is required and decides which day is described.
 */
fun Instant.dayOfMonthSuffix(zone: ZoneId): String = atZone(zone).dayOfMonth.dayOfMonthSuffix()

fun Int.dayOfMonthSuffix(): String {
    if (this in 11..13) {
        return "th"
    }
    return when (this % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

private val REGEX_DAY_PATTERN = "d(\\s|$)".toRegex()

private fun String.withDaySuffix(daySuffix: String): String = replace(REGEX_DAY_PATTERN, "d'" + daySuffix + "'$1")

/**
 * [pattern] with this date's ordinal suffix baked in as a literal after the day-of-month element,
 * e.g. `"d MMM yyyy"` becomes `"d'st' MMM yyyy"` for the 1st of the month.
 */
fun LocalDate.patternWithDaySuffix(pattern: String): String = pattern.withDaySuffix(dayOfMonthSuffix())

/**
 * [pattern] with this date-time's ordinal suffix baked in as a literal after the day-of-month element,
 * e.g. `"d MMM yyyy"` becomes `"d'st' MMM yyyy"` for the 1st of the month.
 */
fun LocalDateTime.patternWithDaySuffix(pattern: String): String = pattern.withDaySuffix(dayOfMonthSuffix())

/**
 * [pattern] with the ordinal suffix of the day this instant falls on in [zone] baked in as a literal
 * after the day-of-month element, e.g. `"d MMM yyyy"` becomes `"d'st' MMM yyyy"` for the 1st of the month.
 *
 * [zone] is required because it is what decides which calendar day - and therefore which suffix - the
 * instant is described by.
 */
fun Instant.patternWithDaySuffix(pattern: String, zone: ZoneId): String = pattern.withDaySuffix(dayOfMonthSuffix(zone))

/**
 * This date formatted with [pattern], with the day of month carrying its English ordinal suffix,
 * e.g. `"d MMM yyyy"` renders as `1st Jan 2024`.
 */
fun LocalDate.formatWithDaySuffix(pattern: String): String = format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern)))

/**
 * This date-time formatted with [pattern], with the day of month carrying its English ordinal suffix,
 * e.g. `"d MMM yyyy HH:mm"` renders as `1st Jan 2024 09:15`.
 */
fun LocalDateTime.formatWithDaySuffix(pattern: String): String = format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern)))

/**
 * This instant formatted with [pattern] as seen from [zone], with the day of month carrying its English
 * ordinal suffix, e.g. `"d MMM yyyy HH:mm"` renders as `1st Jan 2024 09:15`.
 *
 * [zone] is required: an instant near midnight is a different calendar day in different zones
 * (`2026-08-31T23:02:00Z` is the 31st of August in UTC and the 1st of September in `Europe/London`), so
 * there is no sensible default that isn't a wrong day for someone.
 */
fun Instant.formatWithDaySuffix(pattern: String, zone: ZoneId): String =
    atZone(zone).format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern, zone)))

fun Duration.toHumanReadableString(): String {
    val duration = this.truncatedTo(ChronoUnit.MILLIS)

    if (duration == Duration.ZERO) {
        return "0ms"
    }

    val elements = listOf(
        "${duration.toDaysPart()}d",
        "${duration.toHoursPart()}h",
        "${duration.toMinutesPart()}m",
        "${duration.toSecondsPart()}s",
        "${duration.toMillisPart()}ms"
    )
    val start = elements.indexOfFirst { it[0] != '0' }
    val end = elements.indexOfLast { it[0] != '0' }
    return elements.subList(start, end + 1).joinToString(" ")
}

fun kotlin.time.Duration.toHumanReadableString(): String = this.toJavaDuration().toHumanReadableString()
