---
sidebar_position: 1
---

# Pebble Views Wiring

## Initialising views
Invirt uses a custom Pebble extension and a series of other components that are initialised at application startup
via the `Invirt()` call. Below are the key components that are bootstrapped when Invirt is initialised.

#### Defining how Pebble templates are loaded
As per [configuration](/docs/api/invirt-core/configuration) documentation, Invirt uses the `InvirtPebbleConfig`
object to define whether Pebble templates are loaded from the classpath or a hot reload directory.

#### Bootstrapping Pebble templates
Invirt exposes a set of [custom context objects](/docs/api/invirt-core/pebble/pebble-context-objects) and
[utility functions](/docs/api/invirt-core/pebble/pebble-functions) which are only available when the `Invirt()` calls
is wired.

#### Setting a global view lens
Http4k requires that view models are rendered using a previously declared (view) lens. Invirt sets this globally
so it can be reused across the framework and your application, and so you don't need to define it in every handler.

#### Wiring cusatom Pebble extensions
[InvirtPebbleConfig.extensions](/docs/api/invirt-core/configuration) allows for wiring your
custom [Pebble extensions](https://pebbletemplates.io/wiki/guide/extending-pebble/).

## Dynamic hot reload
You can easily configure Invirt to use hot reload locally and caching classpath in production by simply
passing the value of an environment variable to `InvirtConfig.developmentMode`.
Invirt does this [by default](/docs/api/invirt-core/configuration#development-mode) by reading the `DEVELOPMENT_MODE` environment variable, but you can override that
behaviour with your custom variable.

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
