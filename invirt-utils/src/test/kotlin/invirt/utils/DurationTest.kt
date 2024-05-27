package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration

class DurationTest : StringSpec({

    "no duration" {
        Duration.ofMillis(0).toHumanReadableString() shouldBe "0ms"
    }

    "sub-millis duration" {
        Duration.ofNanos(56).toHumanReadableString() shouldBe "0ms"
        Duration.ofNanos(213_210).toHumanReadableString() shouldBe "0ms"
        Duration.ofNanos(999_999).toHumanReadableString() shouldBe "0ms"
        Duration.ofNanos(1_000_000).toHumanReadableString() shouldBe "1ms"
    }

    "human readable string - round number duration" {
        Duration.ofDays(16).toHumanReadableString() shouldBe "16d"
        Duration.ofHours(23).toHumanReadableString() shouldBe "23h"
        Duration.ofMinutes(49).toHumanReadableString() shouldBe "49m"
        Duration.ofSeconds(5).toHumanReadableString() shouldBe "5s"
        Duration.ofMillis(762).toHumanReadableString() shouldBe "762ms"
    }

    "human readable string - various durations" {
        Duration.ofDays(16)
            .plus(Duration.ofHours(23))
            .toHumanReadableString() shouldBe "16d 23h"

        Duration.ofDays(156)
            .plus(Duration.ofHours(23))
            .plus(Duration.ofMinutes(45))
            .toHumanReadableString() shouldBe "156d 23h 45m"

        Duration.ofDays(29)
            .plus(Duration.ofHours(23))
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .toHumanReadableString() shouldBe "29d 23h 45m 12s"

        Duration.ofDays(5)
            .plus(Duration.ofHours(23))
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "5d 23h 45m 12s 345ms"

        Duration.ofHours(23)
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "23h 45m 12s 345ms"

        Duration.ofMinutes(45)
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "45m 12s 345ms"

        Duration.ofSeconds(12)
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "12s 345ms"

        Duration.ofMillis(345).toHumanReadableString() shouldBe "345ms"
    }

    "human readable string - prints zeros between non zeros" {
        Duration.ofDays(7)
            .plus(Duration.ofMinutes(45))
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "7d 0h 45m 12s 345ms"

        Duration.ofDays(16)
            .plus(Duration.ofSeconds(12))
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "16d 0h 0m 12s 345ms"

        Duration.ofDays(23)
            .plus(Duration.ofMillis(345))
            .toHumanReadableString() shouldBe "23d 0h 0m 0s 345ms"

        Duration.ofHours(23)
            .plus(Duration.ofSeconds(27))
            .toHumanReadableString() shouldBe "23h 0m 27s"
    }
})
