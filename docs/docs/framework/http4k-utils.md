---
sidebar_position: 1
---

# Http4k utilities
These are (thin) wrappers over some existing capabilities in the frameworks used by Invirt,
aimed to make it easier to write handlers and views using Pebble templates in and http4k application.

# ViewResponse
A default implementation for the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/) interface
in http4k, to allow passing the template name as a constructor argument and avoid having to implement `fun template()`
with each view model.

```kotlin
data class ListUsersResponse(
    val users: List<User>
): ViewResponse("users/list-users") // resources/webapp/views/users/list-users.peb
```

# ViewResponse rendering
These utilities rely on a default view lens set using the `setDefaultViewLens()` function.

```kotlin
"/users/list" GET {
    ...
    ListUsersResponse(users).ok()
}

"/users/create" POST {
    ...
    CreateUserResponse(user).status(Status.ACCEPTED)
}
```

# Shorthand route binding
```kotlin
"/test" GET { ... },
"/test" POST { ... },
"/test" PUT { ... },
"/test" DELETE { ... },
```
