---
sidebar_position: 3
---

# Date and time

### Day-of-month suffix
```kotlin
1.dayOfMonthSuffix()   // "st"
2.dayOfMonthSuffix()   // "nd"
3.dayOfMonthSuffix()   // "rd"
4.dayOfMonthSuffix()   // "th"
11.dayOfMonthSuffix()  // "th"

LocalDate.of(2026, 3, 1)
    .formatWithDaySuffix("EEEE, MMMM d yyyy")
// "Sunday, March 1st 2026"
```

A matching Pebble filter is registered as
[`dateWithDaySuffix`](/docs/api/invirt-core/pebble-functions#datewithdaysuffix-filter).

### Instant arithmetic
```kotlin
Instant.now().plusDays(7)
Instant.now().minusDays(7)
```

### Human-readable durations
```kotlin
Duration.ofMillis(0).toHumanReadableString()              // "0ms"
Duration.ofSeconds(125).toHumanReadableString()           // "2m 5s"
Duration.ofMillis(3_600_500).toHumanReadableString()      // "1h 0m 0s 500ms"
1.5.seconds.toHumanReadableString()                       // "1s 500ms"
```

Trailing and leading zero units are stripped, so a 90-second duration formats as `1m 30s` rather than
`0h 1m 30s 0ms`.
