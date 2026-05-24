---
sidebar_position: 9
---

# Classpath resources

Loaders for files on the classpath, returning the content in the form most useful at the call site.

```kotlin
val text: String       = resourceAsString("templates/email.html")
val lines: List<String>= resourceAsStrings("data/cities.txt")  // blank lines dropped
val props: Properties  = resourceAsProps("config.properties")
val input: InputStream = resourceAsInput("static/logo.svg")
```

All functions throw if the resource cannot be found.
