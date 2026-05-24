---
sidebar_position: 6
---

# Enums

### valueOfOrNull
Like `enumValueOf<E>(name)` but returns `null` for unknown names instead of throwing.

```kotlin
enum class Status { DRAFT, PUBLISHED, ARCHIVED }

valueOfOrNull<Status>("PUBLISHED")  // Status.PUBLISHED
valueOfOrNull<Status>("missing")    // null
```

### String?.toEnumValues
Parses a comma-separated string of enum names into a list of values. Blank/null input returns an empty
list.

```kotlin
"DRAFT,PUBLISHED".toEnumValues<Status>() // [DRAFT, PUBLISHED]
" DRAFT , ARCHIVED ".toEnumValues<Status>() // trims each element
null.toEnumValues<Status>() // []
```
