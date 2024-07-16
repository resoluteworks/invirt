---
sidebar_position: 2
---

# Pebble Views Wiring

## Initialising views
Invirt uses a custom Pebble extension and a series of other components that must be initialised explicitly via
`initialiseInvirtViews()` at application startup. Below is the function's full signature and the details of the initialisation
steps being performed is discussed in following sub-sections.

```kotlin
fun initialiseInvirtViews(
    hotReload: Boolean = false,
    staticAssetsVersion: String? = null,
    classpathLocation: String = "webapp/views",
    hotReloadDirectory: String = "src/main/resources/webapp/views",
    pebbleExtensions: List<Extension> = emptyList()
)
```

#### Defining how Pebble templates are loaded
This is done via the `hotReload`, `classpathLocation` and `hotReloadDirectory` arguments.

When `hotReload` is `true` the specified `hotReloadDirectory` is used to look-up templates and any template edits
are immediately visible (for example via a browser refresh). This is typically useful in a development
environment

When `hotReload` is `false` the `classpathLocation` is used with additional caching capabilities
using http4k's built-in components (no magic, really). Typically used when deploying
the application in production.

#### Bootstrapping Pebble templates
Invirt exposes a set of [custom context objects](/docs/api/pebble/pebble-context-objects) and [utility functions](/docs/api/pebble/pebble-functions)
which are delivered using a custom implementation of template rendering, wired in this call.

#### Setting a global view lens
Http4k requires that view models are rendered using a previously declared (view) lens. Invirt sets this globally
so it can be reused across the framework and your application.

#### Defining static assets version
The `staticAssetsVersion` argument is required when you need to version static resources (JS/CSS/etc), which is a
common practice in modern web applications. Asset versioning is discussed in detail [here](/docs/framework/static-assets).

#### Additional Pebble extensions
The `pebbleExtensions` argument allows for wiring your custom [Pebble extensions](https://pebbletemplates.io/wiki/guide/extending-pebble/)
should you need to expose custom Pebble capabilities to your template rendering.

## Dynamic hot reload
You can easily configure Invirt to use hot reload locally and caching classpath in production by simply
using an environment variable to pass as the `hotReload` value to `initialiseInvirtViews()`. This can keep
the code simple and flexible.

Invirt also provides a built-in for this purpose to make this wiring even easier.
```kotlin
val developmentMode = Environment.ENV.developmentMode
initialiseInvirtViews(hotReload = developmentMode)
```

`Environment.ENV.developmentMode` reads an environment variable `DEVELOPMENT_MODE` which can be
set on your local machine when running the application to take advantage of hot reload capabilities
(browser refresh loads template edits).

`Environment.ENV.developmentMode` defaults to `false` so in a production environment its absence implicitly
enables the loading of templates from classpath with caching, and no further code changes are required.


## ViewResponse
`ViewResponse` implements the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/) interface
in http4k and allows passing the template name as a constructor argument, in order to avoid having
to implement `ViewModel.template()` every time.

To lookup and render the template, the framework will use the settings and components previously bootstrapped
with `initialiseInvirtViews()`. Together with a few Invirt utility functions, this allows the handler and
view model code to be kept relatively simple.

```kotlin
data class ListUsersResponse(
    val users: List<User>
): ViewResponse("users/list")

val handler =  routes(
    "/users/list" GET {
        ...
        ListUsersResponse(users).ok()
    },
    "/users/create" POST {
        ...
        CreateUserResponse(user).status(Status.ACCEPTED)
    }
)
```
