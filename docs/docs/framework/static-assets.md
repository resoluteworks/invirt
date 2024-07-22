---
sidebar_position: 2
---

# Static Assets

:::note [Example application](https://github.com/resoluteworks/invirt/tree/main/examples/static-assets)
:::


Http4k provides out of the box a set of components for [serving static assets](https://www.http4k.org/guide/reference/core/#serving_static_assets)
and Invirt only adds a few utilities to make caching and versioning easier.

```kotlin
val assetsVersion = gitCommitId()
val developmentMode = Environment.ENV.developmentMode

initialiseInvirtViews(
    hotReload = developmentMode,
    staticAssetsVersion = assetsVersion
)

val appHandler = InvirtFilter().then(
    routes(
        "/static/${assetsVersion}" bind cacheDays(365).then(staticAssets(developmentMode))
    )
)
```

### Assets version
The first key element here is `assetsVersion` which is passed to the `initialiseInvirtViews()`, discussed previously in
[Pebble Views Wiring](/docs/framework/views-wiring). Using this value for the `"/static/${assetsVersion}"` route
then allows the Pebble template code to include assets dynamically, as per example below.
```html
<script src="/static/{{ staticAssetsVersion }}/app.js"></script>
```
`staticAssetsVersion` is a global Pebble templates variable exposed by Invirt, and is read from the
value set when calling `initialiseInvirtViews()`

It's possible to use the Git commit id to version static assets, which is achieved by a call to `gitCommitId()`,
as per example above. This function is discussed in detail [here](/docs/api/invirt-core/environment#gitcommitid).

### Caching
The function `cacheDays(days)` is a simple wrapper over http4k's built-in caching filter and only exists
as a convenience over the construct below.
```kotlin
CachingFilters.Response.MaxAge(Clock.systemUTC(), Duration.ofDays(days.toLong()))
```

### Hot reload vs classpath loading
The function `staticAssets()` is a simple wrapper over http4k's components, that allows easily switching between
hot reload and caching classpath loading of static assets, similarly to how
[views are loaded](/docs/framework/views-wiring#dynamic-hot-reload).
```kotlin
fun staticAssets(
    hotReload: Boolean,
    classpathLocation: String = "webapp/static",
    directory: String = "src/main/resources/webapp/static"
): RoutingHttpHandler
```
