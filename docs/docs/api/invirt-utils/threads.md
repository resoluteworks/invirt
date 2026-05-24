---
sidebar_position: 8
---

# Threads

### ThreadLocal.withValue
Sets a value on the `ThreadLocal` for the duration of the block, and clears it afterwards.

```kotlin
val tenant = ThreadLocal<String>()

tenant.withValue("acme") {
    // tenant.get() == "acme" here
    runQuery()
}
// tenant is cleared regardless of how the block exited
```

### ThreadPool
A small `AutoCloseable` wrapper around a fixed thread pool that tracks submitted futures and waits for
them on `close()`. Intended for short-lived parallel work in scripts and one-shot jobs.

```kotlin
ThreadPool<Int>(threadCount = 4).use { pool ->
    val futures = (1..10).map { i -> pool.submit { compute(i) } }
    val results = futures.map { it.get() }
}

// Or with a graceful shutdown
val pool = ThreadPool<Unit>(2)
pool.shutdown(timeout = 30, unit = TimeUnit.SECONDS)
```
