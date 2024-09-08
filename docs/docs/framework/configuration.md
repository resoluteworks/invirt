---
sidebar_position: 0
---

# Configuration

As we've seen in the [Quickstart](/docs/overview/quickstart) example, a simple call to `Invirt()` wires
the framework with its default configuration. However, you can customise the behaviour of the framework
by passing an `InvirtConfig` object to this initialisation call.

The Invirt configuration object controls how the framework behaves in different environments, and allows you to customise
the Pebble template engine used by Invirt.

Below is the configuration object with its default values. There are a set of sensible defaults which
you can override as needed.

```kotlin
data class InvirtConfig(
    val developmentMode: Boolean = Environment.ENV.developmentMode,
    val pebble: InvirtPebbleConfig = InvirtPebbleConfig()
)

data class InvirtPebbleConfig(
    val classpathLocation: String = "webapp/views",
    val hotReloadDirectory: String = "src/main/resources/webapp/views",
    val extensions: List<Extension> = emptyList(),
    val globalVariables: Map<String, Any> = emptyMap()
)
```

### Development mode
The flag `InvirtConfig.developmentMode` is used to enable hot reload capabilities when running the application locally.
By default, this flag is read from the `DEVELOPMENT_MODE` environment variable as discussed
[here](/docs/api/invirt-core/environment#environmentdevelopmentmode).

When set to `true`, the framework will look for templates in the `InvirtPebbleConfig.hotReloadDirectory` path,
and any template edits will be immediately visible (for example via a browser refresh). This is typically useful
on a local development environment.

When set to `false`, the framework will look for templates in the `InvirtPebbleConfig.classpathLocation` path
with additional caching capabilities using http4k's built-in components. This is typically used when deploying the application in production.

### Pebble configuration
The `InvirtPebbleConfig` object allows you to customise the Pebble template engine used by Invirt.

- `classpathLocation` is the path where the framework will look for templates when `developmentMode` is `false`.
- `hotReloadDirectory` is the path where the framework will look for templates when `developmentMode` is `true`.
- `extensions` is a list of custom Pebble extensions that you can use to expose custom Pebble capabilities to your template rendering.
  See this [example](https://github.com/resoluteworks/invirt/blob/main/examples/security-authentication/src/main/kotlin/examples/authentication/Application.kt#L34)
  for an application using a custom Pebble extension.
- `globalVariables` is a map of global variables that will be available in all Pebble templates. Particularly useful for exposing
  application-wide configuration or constants like a [static assets](/docs/framework/static-assets) version, for example.
