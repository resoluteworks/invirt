---
sidebar_position: 2
---

import quickStartHome from './assets/quickstart-home.png';

# Quick Start

[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/quickstart)

### Dependencies
Invirt comes as a set of libraries, discussed later in this documentation, and which can be added incrementally
as you expand your application's design. Most of the web-tier functionality is contained
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
class IndexResponse(val currentUsername: String) : InvirtView("index")

class Application {

    fun start() {
        val appHandler = routes(
            "/" GET { request ->
                IndexResponse(currentUsername = "email@test.com").ok(request)
            }
        )

        val server = Netty(8080).toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port()}" }
    }
}
```

The content of the `index.peb` template is as follows:

```html
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8"/>
    </head>

    <body style="padding: 40px;">
        <h1>Homepage</h1>
        <div>Current user is <b>{{ model.currentUsername }}</b></div>
    </body>
</html>
```

Opening the browser at http://localhost:8080 will render this template with the `currentUsername` value
as per screenshot below.

<img src={quickStartHome} width="400"/>


### Wiring explained
Invirt is initialised as a singleton via `Invirt.configure(...)`. The first time `Invirt` is referenced
the framework auto-configures itself with sensible defaults, so for the simplest applications no
explicit call is required. Calling `Invirt.configure(...)` explicitly (typically at startup) lets you
override development mode and customise the Pebble template engine.

```kotlin
Invirt.configure(
    developmentMode = Environment.ENV.developmentMode,
    pebble = InvirtPebbleConfig(
        extensions = listOf(/* custom Pebble extensions */),
        globalVariables = mapOf("staticAssetsVersion" to gitCommitId())
    )
)
```

The configuration object is discussed in the [Configuration](/docs/framework/configuration) section.

We also recommend reading about http4k's [templating capabilities](https://www.http4k.org/guide/howto/use_a_templating_engine/),
as Invirt's view rendering is built on top of them.
