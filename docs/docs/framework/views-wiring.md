---
sidebar_position: 1
---

# Views wiring
The core http4k wiring for using a [templating engine](https://www.http4k.org/guide/howto/use_a_templating_engine/),
requires that view models are explicitly rendered using a previously declared view lens. Invirt provides a
set of utilities and wrappers to make it more convenient to write handlers that produce view model responses.

```kotlin
val view = Body.viewModel(renderer, TEXT_HTML).toLens()
...
routes(
    "/test" GET {
        ...
        Response(OK).with(view of viewModel)
    }
)
```
