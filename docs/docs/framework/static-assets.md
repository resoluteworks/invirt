---
sidebar_position: 2
---

# Static Assets

[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/static-assets)

Http4k provides out of the box a set of components for [serving static assets](https://www.http4k.org/guide/reference/core/#serving_static_assets)
and Invirt only adds a few utilities to make caching and versioning easier.

```kotlin
val staticAssetsVersion = gitCommitId()!!
val devMode = Environment.ENV.developmentMode

val config = InvirtConfig(
    developmentMode = devMode,
    pebble = InvirtPebbleConfig(
        globalVariables = mapOf("staticAssetsVersion" to staticAssetsVersion)
    )
)
val appHandler = Invirt(config).then(
    routes(
        "/" GET { renderTemplate("index") },
        "/static/${staticAssetsVersion}" bind cacheDays(365).then(staticAssets(devMode))
    )
)
```

### Assets version
The first key element here is `staticAssetsVersion` which is passed as a global variable in the `InvirtPebbleConfig` setup.
As a Pebble variable, this value can then be used directly in a Pebble template to include assets dynamically, as per example below.
```html
<script src="/static/{{ staticAssetsVersion }}/app.js"></script>
```

In the above example we use the Git commit id to version static assets, which is achieved by a call to
Invirt's `gitCommitId()`. This function is discussed in detail [here](/docs/api/invirt-core/environment#gitcommitid).

### Caching
The function `cacheDays(days)` is a simple wrapper over http4k's built-in caching filter and only exists
as a convenience over the construct below.
```kotlin
CachingFilters.CacheResponse.MaxAge(Duration.ofDays(days.toLong()))
```

### Hot reload vs classpath loading
All the function `staticAssets()` does is to wrap http4k's static assets wiring, and allows easily switching between
hot reload and caching classpath loading of these assets, similarly to how
[views are loaded](http://localhost:3000/docs/framework/configuration#development-mode).
```kotlin
fun staticAssets(
    developmentMode: Boolean,
    classpathLocation: String = "webapp/static",
    directory: String = "src/main/resources/webapp/static"
): RoutingHttpHandler = if (developmentMode) {
    static(ResourceLoader.Directory(directory))
} else {
    static(ResourceLoader.Classpath(classpathLocation))
}
```
