package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateTimeTest : StringSpec({

    "Int.dayOfMonthSuffix" {
        1.dayOfMonthSuffix() shouldBe "st"
        2.dayOfMonthSuffix() shouldBe "nd"
        3.dayOfMonthSuffix() shouldBe "rd"
        11.dayOfMonthSuffix() shouldBe "th"
        17.dayOfMonthSuffix() shouldBe "th"
        23.dayOfMonthSuffix() shouldBe "rd"
    }

    "TemporalAccessor.dayOfMonthSuffix" {
        LocalDate.of(2023, 1, 1).dayOfMonthSuffix() shouldBe "st"
        LocalDate.of(2023, 1, 2).dayOfMonthSuffix() shouldBe "nd"
        LocalDate.of(2023, 1, 3).dayOfMonthSuffix() shouldBe "rd"
        LocalDate.of(2023, 1, 11).dayOfMonthSuffix() shouldBe "th"
        LocalDate.of(2023, 1, 17).dayOfMonthSuffix() shouldBe "th"

        LocalDateTime.of(2023, 1, 1, 23, 10, 15).dayOfMonthSuffix() shouldBe "st"
        LocalDateTime.of(2023, 1, 2, 23, 10, 15).dayOfMonthSuffix() shouldBe "nd"
        LocalDateTime.of(2023, 1, 3, 23, 10, 15).dayOfMonthSuffix() shouldBe "rd"
        LocalDateTime.of(2023, 1, 11, 23, 10, 15).dayOfMonthSuffix() shouldBe "th"
        LocalDateTime.of(2023, 1, 17, 23, 10, 15).dayOfMonthSuffix() shouldBe "th"

        Instant.parse("2024-06-13T09:49:24.245Z").dayOfMonthSuffix() shouldBe "th"
        Instant.parse("2024-07-22T09:49:24.245Z").dayOfMonthSuffix() shouldBe "nd"
    }

    "formatWithDaySuffix" {
        LocalDate.of(2024, 1, 1).formatWithDaySuffix("d MMM YYYY") shouldBe "1st Jan 2024"
        LocalDate.of(2024, 1, 2).formatWithDaySuffix("d MMM YYYY") shouldBe "2nd Jan 2024"
        LocalDate.of(2024, 1, 3).formatWithDaySuffix("d MMM YYYY") shouldBe "3rd Jan 2024"
        LocalDate.of(2024, 1, 11).formatWithDaySuffix("d MMM YYYY") shouldBe "11th Jan 2024"
        LocalDate.of(2024, 1, 17).formatWithDaySuffix("d MMM YYYY") shouldBe "17th Jan 2024"

        LocalDateTime.of(2024, 1, 1, 23, 10, 15).formatWithDaySuffix("d MMM YYYY HH:mm:ss") shouldBe "1st Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 2, 23, 10, 15).formatWithDaySuffix("d MMM YYYY HH:mm:ss") shouldBe "2nd Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 3, 23, 10, 15).formatWithDaySuffix("d MMM YYYY HH:mm:ss") shouldBe "3rd Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 11, 23, 10, 15).formatWithDaySuffix("d MMM YYYY HH:mm:ss") shouldBe "11th Jan 2024 23:10:15"
        LocalDateTime.of(2024, 1, 17, 23, 10, 15).formatWithDaySuffix("d MMM YYYY HH:mm:ss") shouldBe "17th Jan 2024 23:10:15"

        Instant.parse("2024-06-13T09:49:24.245Z").formatWithDaySuffix("d MMM YYYY HH:mm:ss") shouldBe "13th Jun 2024 09:49:24"
    }

    "patternWithDaySuffix" {
        LocalDate.of(2024, 1, 1).patternWithDaySuffix("dd MM YYYY") shouldBe "dd'st' MM YYYY"
        LocalDate.of(2024, 1, 2).patternWithDaySuffix("dd MM YYYY") shouldBe "dd'nd' MM YYYY"
        LocalDate.of(2024, 1, 3).patternWithDaySuffix("d") shouldBe "d'rd'"
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
