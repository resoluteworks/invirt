---
sidebar_position: 1
---

# Route Binding
Convenience utilities for http4k route binding.

```kotlin
// Equivalent of "/test" bind Method.GET to { ... }
"/test" GET { ... }

// Equivalent of "/test" bind Method.POST to { ... }
"/test" POST { ... }

// Equivalent of "/test" bind Method.PUT to { ... }
"/test" PUT { ... }

// Equivalent of "/test" bind Method.DELETE to { ... }
"/test" DELETE { ... }
```
