---
sidebar_position: 1
---

# ViewResponse

`ViewResponse` implements the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/) interface
in http4k and allows passing the template name as a constructor argument, in order to avoid having
to implement `ViewModel.template()` every time.

To lookup and render the template, the framework will use the settings and components previously bootstrapped
when initialising Invirt via the `Invirt()` filter wiring. Together with a few Invirt utility functions,
this allows the handler and view model code to be kept relatively simple.

```kotlin
data class ListUsersResponse(
    val users: List<User>
): ViewResponse("users/list") // Points to the template `users/list.peb`

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
