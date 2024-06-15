---
sidebar_position: 1
---

# Views wiring
The core http4k wiring for using a [templating engine](https://www.http4k.org/guide/howto/use_a_templating_engine/),
requires that view models are explicitly rendered using a previously declared view lens.

It also requires a view response to implement [ViewModel.template()](https://www.http4k.org/api/org.http4k.template/-view-model/)
in order to override the location/name of the template to be used for rendering the view (otherwise defaulting to
a file name derived from the `ViewModel` implementation class name).

Invirt provides a set of utilities and wrappers to make it more convenient to write handlers that produce view model responses,
as well as using a globally defined view lens, used implicitly throughout the framework.

# ViewResponse
`ViewResponse` implements the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/) interface
in http4k and allows passing the template name as a constructor argument, in order to avoid having
to implement `ViewModel.template()` explicitly with every view model object.

```kotlin
data class ListUsersResponse(
    val users: List<User>
): ViewResponse("users/list") // resources/webapp/views/users/list.peb
```
