package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration
import kotlin.time.toKotlinDuration

class DurationTest : StringSpec({

    "no duration" {
        Duration.ofMillis(0) humanReadableShouldBe "0ms"
    }

    "sub-millis duration" {
        Duration.ofNanos(56) humanReadableShouldBe "0ms"
        Duration.ofNanos(213_210) humanReadableShouldBe "0ms"
        Duration.ofNanos(999_999) humanReadableShouldBe "0ms"
        Duration.ofNanos(1_000_000) humanReadableShouldBe "1ms"
    }

    "human readable string - round number duration" {
        Duration.ofDays(16) humanReadableShouldBe "16d"
        Duration.ofHours(23) humanReadableShouldBe "23h"
        Duration.ofMinutes(49) humanReadableShouldBe "49m"
        Duration.ofSeconds(5) humanReadableShouldBe "5s"
        Duration.ofMillis(762) humanReadableShouldBe "762ms"
    }

    "human readable string - various durations" {
        Duration.ofDays(16).plus(Duration.ofHours(23)) humanReadableShouldBe "16d 23h"

        Duration.ofDays(156)
            .plus(Duration.ofHours(23))
            .plus(Duration.ofMinutes(45)) humanReadableShouldBe "156d 23h 45m"

        Duration.ofDays(29)
            .plus(Duration.ofHours(23))
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12)) humanReadableShouldBe "29d 23h 45m 12s"

        Duration.ofDays(5)
            .plus(Duration.ofHours(23))
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "5d 23h 45m 12s 345ms"

        Duration.ofHours(23)
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "23h 45m 12s 345ms"

        Duration.ofMinutes(45)
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "45m 12s 345ms"

        Duration.ofSeconds(12)
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "12s 345ms"

        Duration.ofMillis(345) humanReadableShouldBe "345ms"
    }

    "human readable string - prints zeros between non zeros" {
        Duration.ofDays(7)
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "7d 0h 45m 12s 345ms"

        Duration.ofDays(16)
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "16d 0h 0m 12s 345ms"

        Duration.ofDays(23)
            .plus(Duration.ofMillis(345)) humanReadableShouldBe "23d 0h 0m 0s 345ms"

        Duration.ofHours(23)
            .plus(Duration.ofSeconds(27)) humanReadableShouldBe "23h 0m 27s"
    }
})

private infix fun Duration.humanReadableShouldBe(expected: String) {
    toHumanReadableString() shouldBe expected
    toKotlinDuration().toHumanReadableString() shouldBe expected
}
