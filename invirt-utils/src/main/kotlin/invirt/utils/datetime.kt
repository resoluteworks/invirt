package invirt.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

/**
 * A pair of dates as one phrase, saying each part only once: a null [to] (how a single-day entry is
 * modelled) or a [to] equal to [from] is that one day; a range inside one calendar year carries the year
 * on its closing date alone ("5th November – 5th December 2026"); a range crossing years spells both out.
 *
 * Both ends are formatted with [formatWithDaySuffix], so the day of month carries its English ordinal
 * suffix. [pattern] is the one that carries the year and [yearlessPattern] the one that drops it - the rule
 * needs two patterns, since no single `DateTimeFormatter` pattern can express it. Use `yyyy` for the year:
 * `YYYY` is the week-based year, which renders a plausible but wrong value on the days around new year.
 * [separator] defaults to a spaced en dash, the typographic range separator.
 */
fun formatDateRange(
    from: LocalDate,
    to: LocalDate?,
    pattern: String = "d MMMM yyyy",
    yearlessPattern: String = "d MMMM",
    separator: String = " \u2013 "
): String = when {
    to == null || to == from -> from.formatWithDaySuffix(pattern)
    from.year == to.year -> "${from.formatWithDaySuffix(yearlessPattern)}$separator${to.formatWithDaySuffix(pattern)}"
    else -> "${from.formatWithDaySuffix(pattern)}$separator${to.formatWithDaySuffix(pattern)}"
}

/**
 * This string as an ISO day (`2026-03-01`), or null when it is absent, blank or not a date at all.
 *
 * The receiver is nullable and the result never throws, for reading a date out of somewhere that holds
 * whatever was last written to it - a half-typed value in an autosaved draft, a query parameter, an
 * imported row - where "not a date" is an answer rather than a failure. Surrounding whitespace is ignored.
 */
fun String?.toLocalDateOrNull(): LocalDate? {
    val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
}

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
