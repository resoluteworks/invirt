---
sidebar_position: 12
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# UriPatternMatcher

A simple URI path matcher with wildcard (`*`) support, useful for filters that need to apply only to a
set of routes. Pattern matching is case-insensitive. A pattern matches when the URI starts with the
pattern's prefix (extended to the path boundary, the query string, or end of string).

```text
"/login"   matches  "/login", "/login?a=b", "/login/me"
"/auth*"   matches  "/authenticate", "/auth/1"
"/auth/*"  matches  "/auth/", "/auth/1"  (but not "/authenticate")
```

The matcher rejects overlapping patterns at construction time (e.g. `/auth` and `/auth/foo`).

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val publicPaths = UriPatternMatcher("/login", "/logout", "/static/*")

    val filter = Filter { next ->
        { request ->
            if (publicPaths.matches(request) || request.hasPrincipal) next(request)
            else Response(Status.FORBIDDEN)
        }
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    data class UriPatternMatcher(val patterns: Set<String>) {
        constructor(vararg patterns: String)
        fun matches(uri: String): Boolean
        fun matches(request: Request): Boolean
    }
    ```
  </TabItem>
</Tabs>
