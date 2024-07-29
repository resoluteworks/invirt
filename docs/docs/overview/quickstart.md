---
sidebar_position: 2
---

# Quick Start

For the in-depth documentation please check:
 * [Framework documentation](/docs/framework/views-wiring)
 * [API documentation](/docs/api/invirt-core/route-binding)

## Quick Start Application
[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/quickstart)

### Dependencies
Invirt comes as a set of libraries, discussed later in this documentation, and which can be added incrementally
as you expand your application's design. Most of the functionality, however, is contained
in the core library which can be added as per Gradle example below.

```kotlin
implementation(platform("dev.invirt:invirt-bom:${invirtVersion}"))
implementation("dev.invirt:invirt-core")
```

You will also need to add the http4k libraries which Invirt relies on. Below is the minimum required
to get started with an Invirt app. Netty is simply used as an example, you can of course choose your
preferred [http4k server backend](https://www.http4k.org/guide/reference/servers/).

```kotlin
implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
implementation("org.http4k:http4k-core")
implementation("org.http4k:http4k-server-netty")
implementation("org.http4k:http4k-config")
implementation("org.http4k:http4k-template-pebble")
```

### Project structure

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

### Application
```kotlin
class IndexResponse(val currentUsername: String) : ViewResponse("index.peb")

class Application {

    fun start() {
        initialiseInvirtViews()

        val appHandler = InvirtFilter().then(
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
`IndexResponse` extends Invirt's `ViewResponse` (a convenience implementation of http4k's [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/)).
This object stores the data to be used in the template (`currentUsername`), and defines the template to be rendered (`index.peb`)

`IndexResponse` is available as the `model` object within the template, as per example below.
```html
<div>
    Current user is {{ model.currentUsername }}
</div>
```

### Wiring explained
In the code above there are two components required to enable Invirt in your http4k application.

#### 1. Initialising Invirt views
```kotlin
initialiseInvirtViews()
```
This sets a default view lens to be used throughout your application when rendering Pebble template responses.
We recommend reading more about http4k's [templating capabilities](https://www.http4k.org/guide/howto/use_a_templating_engine/), most of Invirt
is built on top of those.

There are several parameters that can be passed to `initialiseInvirtViews()` to override the default behaviour.
All of these are discussed in detail in [Pebble Views Wiring](/docs/framework/views-wiring).

#### 2. InvirtFilter
```kotlin
val appHandler = InvirtFilter()
    .then(routes(...))
```
`InvirtFilter` handles a few of the framework's internals, including setting the current http4k `Request`
on the current thread, as well as managing validation errors for a request. These are in turn exposed internally
to other Invirt components and your application. You can add this filter anywhere in your application's filter chain
before wiring your http4k routes.
