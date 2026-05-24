---
sidebar_position: 7
---

# Files

### workingDirectory / tempDirectory
```kotlin
workingDirectory() // File for System.getProperty("user.dir")
tempDirectory()    // File for System.getProperty("java.io.tmpdir")
```

### TempDir
A `Closeable` wrapper around a freshly created temporary directory. The directory and its contents
are deleted on `close()`. Useful in tests and one-shot scripts.

```kotlin
TempDir().use { tmp ->
    val file = tmp.newFile("json")  // <tmp>/<uuid7>.json
    val dir  = tmp.newDirectory()   // <tmp>/<uuid7>/
    // ...
}

// Or via the inline helper
withTempDir { tmp ->
    // ...
}
```
