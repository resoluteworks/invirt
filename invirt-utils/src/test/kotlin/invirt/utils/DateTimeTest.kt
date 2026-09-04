package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class DateTimeTest : StringSpec({

    val utc = ZoneId.of("UTC")
    val london = ZoneId.of("Europe/London")
    val newYork = ZoneId.of("America/New_York")

    "Int.dayOfMonthSuffix" {
        1.dayOfMonthSuffix() shouldBe "st"
        2.dayOfMonthSuffix() shouldBe "nd"
        3.dayOfMonthSuffix() shouldBe "rd"
        4.dayOfMonthSuffix() shouldBe "th"
        // 11-13 are the exception: they take "th" even though they end in 1, 2 and 3
        11.dayOfMonthSuffix() shouldBe "th"
        12.dayOfMonthSuffix() shouldBe "th"
        13.dayOfMonthSuffix() shouldBe "th"
        // and 21-23 go back to following the last digit
        17.dayOfMonthSuffix() shouldBe "th"
        21.dayOfMonthSuffix() shouldBe "st"
        22.dayOfMonthSuffix() shouldBe "nd"
        23.dayOfMonthSuffix() shouldBe "rd"
        31.dayOfMonthSuffix() shouldBe "st"
    }

    "LocalDate.dayOfMonthSuffix" {
        LocalDate.of(2023, 1, 1).dayOfMonthSuffix() shouldBe "st"
        LocalDate.of(2023, 1, 2).dayOfMonthSuffix() shouldBe "nd"
        LocalDate.of(2023, 1, 3).dayOfMonthSuffix() shouldBe "rd"
        LocalDate.of(2023, 1, 11).dayOfMonthSuffix() shouldBe "th"
        LocalDate.of(2023, 1, 17).dayOfMonthSuffix() shouldBe "th"
    }

    "LocalDateTime.dayOfMonthSuffix" {
        LocalDateTime.of(2023, 1, 1, 23, 10, 15).dayOfMonthSuffix() shouldBe "st"
        LocalDateTime.of(2023, 1, 2, 23, 10, 15).dayOfMonthSuffix() shouldBe "nd"
        LocalDateTime.of(2023, 1, 3, 23, 10, 15).dayOfMonthSuffix() shouldBe "rd"
        LocalDateTime.of(2023, 1, 11, 23, 10, 15).dayOfMonthSuffix() shouldBe "th"
        LocalDateTime.of(2023, 1, 17, 23, 10, 15).dayOfMonthSuffix() shouldBe "th"
    }

    "Instant.dayOfMonthSuffix" {
        Instant.parse("2024-06-13T09:49:24.245Z").dayOfMonthSuffix(utc) shouldBe "th"
        Instant.parse("2024-07-22T09:49:24.245Z").dayOfMonthSuffix(utc) shouldBe "nd"

        // 23:02 UTC on the 2nd is 00:02 on the 3rd in London (BST), so the zone picks the suffix
        val secondOfAugust = Instant.parse("2026-08-02T23:02:00Z")
        secondOfAugust.dayOfMonthSuffix(utc) shouldBe "nd"
        secondOfAugust.dayOfMonthSuffix(london) shouldBe "rd"
    }

    "LocalDate.formatWithDaySuffix" {
        LocalDate.of(2024, 1, 1).formatWithDaySuffix("d MMM yyyy") shouldBe "1st Jan 2024"
        LocalDate.of(2024, 1, 2).formatWithDaySuffix("d MMM yyyy") shouldBe "2nd Jan 2024"
        LocalDate.of(2024, 1, 3).formatWithDaySuffix("d MMM yyyy") shouldBe "3rd Jan 2024"
        LocalDate.of(2024, 1, 11).formatWithDaySuffix("d MMM yyyy") shouldBe "11th Jan 2024"
        LocalDate.of(2024, 1, 17).formatWithDaySuffix("d MMM yyyy") shouldBe "17th Jan 2024"
    }

    "LocalDateTime.formatWithDaySuffix" {
        LocalDateTime.of(2024, 1, 1, 23, 10, 15).formatWithDaySuffix("d MMM yyyy HH:mm:ss") shouldBe "1st Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 2, 23, 10, 15).formatWithDaySuffix("d MMM yyyy HH:mm:ss") shouldBe "2nd Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 3, 23, 10, 15).formatWithDaySuffix("d MMM yyyy HH:mm:ss") shouldBe "3rd Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 11, 23, 10, 15).formatWithDaySuffix("d MMM yyyy HH:mm:ss") shouldBe "11th Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 17, 23, 10, 15).formatWithDaySuffix("d MMM yyyy HH:mm:ss") shouldBe "17th Jan 2024 23:10:15"
    }

    "Instant.formatWithDaySuffix" {
        Instant.parse("2024-06-13T09:49:24.245Z").formatWithDaySuffix("d MMM yyyy HH:mm:ss", utc) shouldBe "13th Jun 2024 09:49:24"

        // 23:02 UTC on the last night of August is already September in London (BST): day and month both move
        val lastNightOfAugust = Instant.parse("2026-08-31T23:02:00Z")
        lastNightOfAugust.formatWithDaySuffix("d MMMM yyyy", utc) shouldBe "31st August 2026"
        lastNightOfAugust.formatWithDaySuffix("d MMMM yyyy", london) shouldBe "1st September 2026"

        // the suffix comes from the day the instant lands on in that zone, not from the UTC day
        val secondOfAugust = Instant.parse("2026-08-02T23:02:00Z")
        secondOfAugust.formatWithDaySuffix("d MMMM yyyy", utc) shouldBe "2nd August 2026"
        secondOfAugust.formatWithDaySuffix("d MMMM yyyy", london) shouldBe "3rd August 2026"

        // a zone behind UTC moves the day the other way
        Instant.parse("2026-09-01T02:30:00Z").formatWithDaySuffix("d MMMM yyyy HH:mm", newYork) shouldBe "31st August 2026 22:30"

        // and the year goes with it
        Instant.parse("2026-12-31T23:30:00Z").formatWithDaySuffix("d MMMM yyyy", ZoneId.of("Europe/Paris")) shouldBe "1st January 2027"
    }

    "Instant.formatWithDaySuffix - daylight saving" {
        // London is BST (UTC+1) in August and GMT (UTC+0) in December
        val august = Instant.parse("2026-08-31T12:00:00Z")
        val december = Instant.parse("2026-12-01T12:00:00Z")

        august.formatWithDaySuffix("d MMMM yyyy HH:mm", london) shouldBe "31st August 2026 13:00"
        december.formatWithDaySuffix("d MMMM yyyy HH:mm", london) shouldBe "1st December 2026 12:00"

        august.formatWithDaySuffix("d MMMM yyyy HH:mm", utc) shouldBe "31st August 2026 12:00"
        december.formatWithDaySuffix("d MMMM yyyy HH:mm", utc) shouldBe "1st December 2026 12:00"
    }

    "patternWithDaySuffix" {
        LocalDate.of(2024, 1, 1).patternWithDaySuffix("dd MM yyyy") shouldBe "dd'st' MM yyyy"
        LocalDate.of(2024, 1, 2).patternWithDaySuffix("dd MM yyyy") shouldBe "dd'nd' MM yyyy"
        LocalDate.of(2024, 1, 3).patternWithDaySuffix("d") shouldBe "d'rd'"

        LocalDateTime.of(2024, 1, 2, 23, 10, 15).patternWithDaySuffix("dd MM yyyy") shouldBe "dd'nd' MM yyyy"
        LocalDateTime.of(2024, 1, 3, 23, 10, 15).patternWithDaySuffix("d") shouldBe "d'rd'"

        val secondOfAugust = Instant.parse("2026-08-02T23:02:00Z")
        secondOfAugust.patternWithDaySuffix("dd MM yyyy", utc) shouldBe "dd'nd' MM yyyy"
        secondOfAugust.patternWithDaySuffix("dd MM yyyy", london) shouldBe "dd'rd' MM yyyy"
    }

    "patternWithDaySuffix - only a day followed by whitespace or end of pattern takes the suffix" {
        val date = LocalDate.of(2026, 9, 1)

        date.patternWithDaySuffix("d MMMM yyyy") shouldBe "d'st' MMMM yyyy"
        date.patternWithDaySuffix("MMMM d") shouldBe "MMMM d'st'"
        date.patternWithDaySuffix("EEEE, d MMMM") shouldBe "EEEE, d'st' MMMM"

        // a day followed by a separator rather than whitespace is left alone, so the pattern renders
        // with no ordinal at all - these formats are not what this function is for
        date.patternWithDaySuffix("d/MM/yyyy") shouldBe "d/MM/yyyy"
        date.patternWithDaySuffix("d-MMM-yyyy") shouldBe "d-MMM-yyyy"
        date.formatWithDaySuffix("d/MM/yyyy") shouldBe "1/09/2026"
    }

    "Instant.plusDays" {
        fun test(year: Int, month: Int, dayOfMonth: Int, addDays: Int, expectedDate: LocalDate) {
            LocalDateTime.ofInstant(
                LocalDateTime.of(year, month, dayOfMonth, 23, 10, 43).toInstant(ZoneOffset.UTC).plusDays(addDays),
                ZoneOffset.UTC
            ).toLocalDate() shouldBe expectedDate
        }
        test(2024, 1, 1, 1, LocalDate.of(2024, 1, 2))
        test(2024, 1, 1, 15, LocalDate.of(2024, 1, 16))
        test(2024, 2, 28, 1, LocalDate.of(2024, 2, 29))
        test(2024, 12, 31, 1, LocalDate.of(2025, 1, 1))
    }

    "Instant.minusDays" {
        fun test(year: Int, month: Int, dayOfMonth: Int, subtractDays: Int, expectedDate: LocalDate) {
            LocalDateTime.ofInstant(
                LocalDateTime.of(year, month, dayOfMonth, 23, 10, 43).toInstant(ZoneOffset.UTC).minusDays(subtractDays),
                ZoneOffset.UTC
            )
                .toLocalDate() shouldBe expectedDate
        }
        test(2024, 1, 1, 1, LocalDate.of(2023, 12, 31))
        test(2024, 1, 15, 14, LocalDate.of(2024, 1, 1))
    }
})
