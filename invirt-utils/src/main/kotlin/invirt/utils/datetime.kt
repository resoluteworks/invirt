package invirt.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import kotlin.time.toJavaDuration

fun Instant.plusDays(days: Int): Instant = this.plus(days.toLong(), ChronoUnit.DAYS)

fun Instant.minusDays(days: Int): Instant = this.minus(days.toLong(), ChronoUnit.DAYS)

fun Temporal.dayOfMonthSuffix(): String = when (this) {
    is Instant -> atZone(ZoneOffset.UTC).dayOfMonth.dayOfMonthSuffix()
    else -> get(ChronoField.DAY_OF_MONTH).dayOfMonthSuffix()
}

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
fun Temporal.patternWithDaySuffix(pattern: String): String = pattern.replace(REGEX_DAY_PATTERN, "d'" + dayOfMonthSuffix() + "'$1")

fun Temporal.formatWithDaySuffix(pattern: String): String = when (this) {
    is LocalDate -> format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern)))
    is LocalDateTime -> format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern)))
    is Instant -> atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern)))
    else -> throw UnsupportedOperationException("Can't handle Temporal of type ${this::class}")
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
