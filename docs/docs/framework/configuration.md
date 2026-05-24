---
sidebar_position: 0
---

# Configuration

`Invirt` is a singleton object. The framework is initialised through `Invirt.configure(...)` which
sets development mode and customises the Pebble template engine used to render views.

```kotlin
object Invirt {

    fun configure(
        developmentMode: Boolean = Environment.ENV.developmentMode,
        pebble: InvirtPebbleConfig = InvirtPebbleConfig()
    )
}
```

The first reference to `Invirt` triggers a default configuration via its `init` block, so for the simplest
applications no explicit call is required. Calling `Invirt.configure(...)` is needed when you want
to override the defaults &mdash; for example to register Pebble extensions or change the template
classpath location. Configuration is global; call `configure(...)` once during application startup,
before serving any requests.

```kotlin
fun main() {
    Invirt.configure(
        developmentMode = Environment.ENV.developmentMode,
        pebble = InvirtPebbleConfig(
            classpathLocation = "webapp/views",
            extensions = listOf(/* ... */),
            globalVariables = mapOf("staticAssetsVersion" to gitCommitId())
        )
    )

    val server = Netty(8080).toServer(routes(/* ... */)).start()
}
```

### Pebble configuration
`InvirtPebbleConfig` holds the settings for the Pebble template engine.

```kotlin
class InvirtPebbleConfig(
    val classpathLocation: String = "webapp/views",
    val hotReloadDirectory: String = "src/main/resources/webapp/views",
    val extensions: List<Extension> = emptyList(),
    val globalVariables: Map<String, Any> = emptyMap(),
    val contextVariables: Map<String, (Request) -> Any?> = emptyMap()
)
```

- `classpathLocation` &mdash; classpath path where templates are loaded from when `developmentMode` is `false`.
- `hotReloadDirectory` &mdash; filesystem path where templates are loaded from when `developmentMode` is `true`.
- `extensions` &mdash; custom Pebble [`Extension`](https://pebbletemplates.io/wiki/guide/extending-pebble/) instances. See the
  [security-authentication example](https://github.com/resoluteworks/invirt/blob/main/examples/security-authentication/src/main/kotlin/examples/authentication/Application.kt)
  for an application using a custom Pebble extension.
- `globalVariables` &mdash; variables available in every Pebble template. Useful for application-wide constants
  like a [static assets](/docs/framework/static-assets) version.
- `contextVariables` &mdash; per-request variables computed from the current `Request` and exposed to the template.
  The lambda is invoked at render time with the request that produced the response.

### Development mode
`developmentMode` controls template loading:

- `true` &mdash; templates are loaded from `hotReloadDirectory` with no caching, so edits to a template are visible immediately
  on a browser refresh. Typically used during local development.
- `false` &mdash; templates are loaded from `classpathLocation` with http4k's caching template loader. Typically used in production.

By default `developmentMode` is read from the `DEVELOPMENT_MODE` environment variable
(see [`Environment.developmentMode`](/docs/api/invirt-core/environment#environmentdevelopmentmode)) and defaults
to `false` when the variable is not set.
