---
sidebar_position: 2
---

# Http4k utilities
These are (thin) wrappers over some existing capabilities in the frameworks used by Invirt,
aimed to make it easier to write handlers and views using Pebble templates in and http4k application.

# Shorthand for route binding
```kotlin
"/test" GET { ... },
"/test" POST { ... },
"/test" PUT { ... },
"/test" DELETE { ... },
```
