package invirt.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Instant.plusDays(days: Int): Instant {
    return this.plus(days.toLong(), ChronoUnit.DAYS)
}

fun Instant.minusDays(days: Int): Instant {
    return this.minus(days.toLong(), ChronoUnit.DAYS)
}

fun LocalDate.dayOfMonthSuffix(): String {
    if (dayOfMonth in 11..13) {
        return "th"
    }
    return when (dayOfMonth % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

private val REGEX_DAY_PATTERN = "d(\\s|$)".toRegex()
fun LocalDate.patternWithDaySuffix(pattern: String): String {
    return pattern.replace(REGEX_DAY_PATTERN, "d'" + dayOfMonthSuffix() + "'$1")
}

fun LocalDate.formatWithDaySuffix(pattern: String): String {
    return format(DateTimeFormatter.ofPattern(patternWithDaySuffix(pattern)))
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
