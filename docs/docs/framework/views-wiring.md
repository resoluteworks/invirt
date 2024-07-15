---
sidebar_position: 2
---

# Pebble views wiring
When using a [templating engine](https://www.http4k.org/guide/howto/use_a_templating_engine/), http4k requires a view model
to implement [ViewModel.template()](https://www.http4k.org/api/org.http4k.template/-view-model/)
in order to override the location of the template to render the view. Invirt provides a [ViewResponse](#viewresponse) class
to make this more convenient.

Http4k also requires that view models are explicitly rendered using a previously declared lens. Invirt allows this be defined
globally and reused using [Default view lens](#default-view-lens).


## ViewResponse
`ViewResponse` implements the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/) interface
in http4k and allows passing the template name as a constructor argument, in order to avoid having
to implement `ViewModel.template()` every time.

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

## ViewResponse to HTTP response
A set of extension functions make use of the previously configured `setDefaultViewLens()` in order to simplify
returning an http4k `Response` directly from a `ViewResponse` object.

```kotlin
setDefaultViewLens(Views(hotReload = ...))

// "users/list" points to
// - classpath:webapp/views/users/list.peb when hotReload is false
// - file:src/main/resources/webapp/views/users/list.peb when hotReload is true
data class ListUsersResponse(
    val users: List<User>
): ViewResponse("users/list")

val handler =  routes(
    "/users/list" GET {
        ...
        ListUsersResponse(users).ok() // Returns a response with Status.OK
    },

    "/users/create" POST {
        ...
        CreateUserResponse(user).status(Status.ACCEPTED) // Returns a response with Status.ACCEPTED
    }
)
```
