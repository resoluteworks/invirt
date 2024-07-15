---
sidebar_position: 1
---

# Quick start

## Dependencies
Invirt comes as a set of libraries, discussed later in this documentation, and which can be added incrementally
as you expand your application's design. Most of the functionality, however, is contained
in the core library which can be added as per Gradle example below.

```kotlin
implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
implementation("io.resoluteworks:invirt-core")
```

You will also need to add the http4k libraries which Invirt relies on. Below is the minimum required
to get started with an Invirt app. Netty is simply used as an example, you can of course choose your
preferred http4k server implementation.

```kotlin
implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
implementation("org.http4k:http4k-core")
implementation("org.http4k:http4k-server-netty")
implementation("org.http4k:http4k-cloudnative")
implementation("org.http4k:http4k-template-pebble")
```

## Project structure

The structure of an Invirt project is similar to any other http4k application, with some built-in defaults
for template look-ups. For a complete example, please check the [Quickstart project](https://github.com/resoluteworks/invirt/tree/main/examples/quickstart).

```text
├── build.gradle.kts
└── src
    └── main
        ├── kotlin
        │   └── examples
        │       └── quickstart
        │           └── Application.kt
        └── resources
            └── webapp
                └── views
                    └── index.peb
```

## Application
A basic setup for an application using Invirt looks something like the one below. Again, this should be very much
in line with any other http4k application, except for a couple of Invirt wiring elements.

```kotlin
class IndexResponse(val currentUsername: String) : ViewResponse("index")

class Application {

    fun start() {
        setDefaultViewLens(Views(hotReload = true))

        val appHandler = InvirtRequestContext()
            .then(
                routes(
                    "/" GET {
                        IndexResponse(currentUsername = "email@test.com").ok()
                    }
                )
            )

        val server = Netty(8080)
        server.toServer(appHandler).start()
    }
}
```

The code above renders the `index.peb` template for the default (`/`) route and sets the template's `model`
to an instance of `IndexResponse`. Below is an example of rendering the `currentUsername` field within
this template, which uses http4k's [native constructs](https://www.http4k.org/guide/reference/templating/#notes_for_pebble)
for accessing view model data in Pebble template.
```html
<div>
    Current user is {{ model.currentUsername }}
</div>
```

## Wiring explained
In the code above there are two components required to enable Invirt in your http4k application.

#### 1. Setting the default view lens
```kotlin
setDefaultViewLens(Views(hotReload = true))
```
This enables a default view lens to be used throughout your application when rendering Pebble template responses.
By default, it looks up templates in `classpath:/webapp/views`. We recommend reading more about http4k's
[templating capabilities](https://www.http4k.org/guide/howto/use_a_templating_engine/), most of Invirt
is built on top of those.

We discuss Invirt views wiring in detail [here](/docs/framework/views-wiring).

#### 2. Wiring the InvirtRequestContext filter
```kotlin
val appHandler = InvirtRequestContext()
    .then(routes(...))
```
`InvirtRequestContext` is a filter responsible for setting the current http4k `Request` on the current thread, as well
as managing validation errors on a request. These are in turn exposed internally to other Invirt components and your application.
You can add this filter anywhere before the wiring of your http4k routes.
