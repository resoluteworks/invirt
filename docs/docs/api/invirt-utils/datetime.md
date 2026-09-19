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

LocalDateTime.of(2026, 3, 1, 9, 15)
    .formatWithDaySuffix("d MMMM yyyy HH:mm")
// "1st March 2026 09:15"
```

`dayOfMonthSuffix`, `patternWithDaySuffix` and `formatWithDaySuffix` all take a `ZoneId` on `Instant`,
because an instant is a point on the timeline and only a zone gives it a calendar day.

```kotlin
val dispatchedAt = Instant.parse("2026-08-31T23:02:00Z")

dispatchedAt.formatWithDaySuffix("d MMMM yyyy", ZoneId.of("UTC"))
// "31st August 2026"

dispatchedAt.formatWithDaySuffix("d MMMM yyyy", ZoneId.of("Europe/London"))
// "1st September 2026"
```

A matching Pebble filter is registered as
[`dateWithDaySuffix`](/docs/api/invirt-core/pebble-functions#datewithdaysuffix-filter).

### Date ranges
`formatDateRange` renders a pair of dates as one phrase that says each part only once: a null `to` (how a
single-day entry is modelled) or a `to` equal to `from` is that one day, a range inside one calendar year
carries the year on its closing date alone, and a range crossing years spells both out. Both ends are
formatted with `formatWithDaySuffix`.

```kotlin
formatDateRange(LocalDate.of(2026, 11, 5), null)
// "5th November 2026"

formatDateRange(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 12, 5))
// "5th November – 5th December 2026"

formatDateRange(LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2))
// "30th December 2026 – 2nd January 2027"
```

The rule needs two patterns, since no single `DateTimeFormatter` pattern can express it: `pattern` carries
the year and `yearlessPattern` drops it. Both, and the separator, are the caller's.

```kotlin
formatDateRange(
    from = LocalDate.of(2026, 11, 5),
    to = LocalDate.of(2026, 12, 5),
    pattern = "d MMM yyyy",
    yearlessPattern = "d MMM",
    separator = " to "
)
// "5th Nov to 5th Dec 2026"
```

Use `yyyy` for the year. `YYYY` is the week-based year, and it renders a plausible but wrong value on the
days around new year: the 30th of December 2026 falls in ISO week-year 2026 along with the 2nd of January
2027, so the range above would read as one year.

A matching Pebble function is registered as
[`dateRange`](/docs/api/invirt-core/pebble-functions#daterangefrom-to).

### Parsing a date that might not be one
`toLocalDateOrNull` reads an ISO day out of somewhere that holds whatever was last written to it - a
half-typed value in an autosaved draft, a query parameter, an imported row - where "not a date" is an
answer rather than a failure. The receiver is nullable and the result never throws.

```kotlin
"2026-03-01".toLocalDateOrNull()   // LocalDate.of(2026, 3, 1)
"  2026-03-01 ".toLocalDateOrNull()  // LocalDate.of(2026, 3, 1)
null.toLocalDateOrNull()           // null
"".toLocalDateOrNull()             // null
"01/03/2026".toLocalDateOrNull()   // null
"2026-02-30".toLocalDateOrNull()   // null
```

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
