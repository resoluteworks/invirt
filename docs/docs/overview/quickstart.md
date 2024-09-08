---
sidebar_position: 2
---

import quickStartHome from './assets/quickstart-home.png';

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
class IndexResponse(val currentUsername: String) : ViewResponse("index")

class Application {

    fun start() {
        val appHandler = Invirt().then(
            routes(
                "/" GET {
                    IndexResponse(currentUsername = "email@test.com").ok()
                }
            )
        )

        val server = Netty(8080)
        server.toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port}" }
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
In order to wire Invirt in your http4k application, you simply define the `Invirt()` filter before your
application's routes. This filter sets a default view lens for your application, and bootstraps other
framework internals discussed later in this documentation.

The behaviour of the Invirt filter can be customised using a configuration object discussed in the
[Configuration API Docs](/docs/api/invirt-core/configuration).

We also recommend reading more about http4k's [templating capabilities](https://www.http4k.org/guide/howto/use_a_templating_engine/),
most of Invirt is built on top of those.

