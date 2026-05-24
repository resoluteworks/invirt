---
sidebar_position: 2
---

# Static Assets

[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/static-assets)

Http4k provides out of the box a set of components for [serving static assets](https://www.http4k.org/guide/reference/core/#serving_static_assets),
and Invirt only adds a few utilities to make caching and versioning easier.

```kotlin
val staticAssetsVersion = gitCommitId()!!
val devMode = Environment.ENV.developmentMode

Invirt.configure(
    developmentMode = devMode,
    pebble = InvirtPebbleConfig(
        globalVariables = mapOf("staticAssetsVersion" to staticAssetsVersion)
    )
)

val appHandler = routes(
    "/" GET { request -> renderTemplate(request, "index") },
    "/static/${staticAssetsVersion}" bind cacheDays(365).then(staticAssets(devMode))
)
```

### Assets version
`staticAssetsVersion` is exposed as a Pebble global variable, so templates can include assets dynamically:
```html
<script src="/static/{{ staticAssetsVersion }}/app.js"></script>
```

In the snippet above we use the Git commit id, retrieved by Invirt's
[`gitCommitId()`](/docs/api/invirt-core/environment#gitcommitid).

### Caching
`cacheDays(days)` is a thin convenience over http4k's built-in caching filter:
```kotlin
CachingFilters.CacheResponse.MaxAge(Duration.ofDays(days.toLong()))
```

A `cacheOneYear()` shorthand is also available for the common case.

### Hot reload vs classpath loading
`staticAssets(...)` wraps http4k's static asset wiring and switches between hot-reload (filesystem) and
caching (classpath) loading, mirroring how
[views are loaded](/docs/framework/configuration#development-mode).
```kotlin
fun staticAssets(
    developmentMode: Boolean,
    classpathLocation: String = "webapp/static",
    directory: String = "src/main/resources/webapp/static"
): RoutingHttpHandler
```
