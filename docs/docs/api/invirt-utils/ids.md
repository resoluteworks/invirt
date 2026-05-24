---
sidebar_position: 5
---

# IDs

### uuid7
Returns a time-ordered UUIDv7 as a dashless hex string. Backed by
[`UuidCreator.getTimeOrderedEpoch`](https://github.com/f4b6a3/uuid-creator).

```kotlin
uuid7() // "01959f3a4b6f7e0d8a1b9f2e5c3a4b6f"
```

### UUID encoding helpers
```kotlin
val uuid = UUID.randomUUID()
uuid.toByteArray()  // 16-byte big-endian representation
uuid.toBase32()     // lowercase base32, no padding

uuidBase32()        // shortcut for UUID.randomUUID().toBase32()
```
