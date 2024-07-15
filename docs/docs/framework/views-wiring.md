---
sidebar_position: 2
---

# Views wiring
The core http4k wiring for using a [templating engine](https://www.http4k.org/guide/howto/use_a_templating_engine/),
requires that view models are explicitly rendered using a previously declared view lens.
It also requires a view response to implement [ViewModel.template()](https://www.http4k.org/api/org.http4k.template/-view-model/)
in order to override the location/name of the template to be used for rendering the view (otherwise defaulting to
a file name derived from the `ViewModel` implementation class name).

Invirt provides a set of utilities and wrappers to make it more convenient to write handlers that produce view model responses,
as well as using a globally defined view lens, used throughout the framework.

## ViewResponse
`ViewResponse` implements the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/) interface
in http4k and allows passing the template name as a constructor argument, in order to avoid having
to implement `ViewModel.template()` explicitly.

```kotlin
data class ListUsersResponse(
    val users: List<User>
): ViewResponse("users/list") // classpath:/webapp/views/users/list.peb
```
## Default view lens
Invirt provides a built-in [Views](https://github.com/resoluteworks/invirt/blob/main/invirt-core/src/main/kotlin/invirt/http4k/views/views.kt#L23)
component to bootstrap some of the core framework capabilities, enable Pebble templates rendering, and configure how the views are loaded.
This component can then be wired at application startup using a `setDefaultViewLens()` call.

```kotlin
// Used for hot reload capabilities, useful at development time
// (browser refresh reloads the Pebble template edits)
setDefaultViewLens(Views.HotReload(directory = "../views"))

// Used in production to load the views from the classpath, with
// caching and no hot reload capabilities
setDefaultViewLens(Views.Classpath(classpathDir = "webapps/views"))

// [RECOMMENDED]
// Can be used in both production and local development with the value
// for hotReload read from an env var
setDefaultViewLens(Views(hotReload = ...))
```

### Configuring views for both local and production
The latter construct uses the defaults defined in the `Views.Classpath` and `Views.HotReload` respectively, which are set to
 * `webapp/views` for classpath views
 * `src/main/resources/webapp/views` for directory (hot reloading) views

With these defaults it's then easy to configure the application to use an environment variable to bootstrap the application
so that it hot reloads locally (a browser refresh renders the updated template), but load the classpath views with caching
at runtime, in production.

```kotlin
val developmentMode = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(Environment.ENV)
setDefaultViewLens(Views(hotReload = developmentMode))
```

## Rendering view model responses
A set of extension functions make use of the previously configured `setDefaultViewLens()` in order to simplify
returning an http4k `Response` directly from a `ViewResponse` object.

```kotlin
val developmentMode = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(Environment.ENV)
setDefaultViewLens(Views(hotReload = developmentMode))

// "users/list" points to
// - classpath:webapp/views/users/list.peb when hotReload is false
// - src/main/resources/webapp/views/users/list.peb when hotReload is true
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
}
```
