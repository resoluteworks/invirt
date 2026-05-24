---
sidebar_position: 1
---

# Overview

Invirt ships a set of test utilities split across modules, each one paired with the production module it
supports. All test modules depend on [Kotest](https://kotest.io/) for matcher styling.

| Dependency | Purpose |
|---|---|
| `dev.invirt:invirt-test` | Building form requests, asserting view responses, validation errors, redirects, cookies. |
| `dev.invirt:invirt-security-test` | Building `Authenticator` test doubles and asserting `AuthenticationResponse` outcomes. |
| `dev.invirt:invirt-mongodb-test` | Spinning up a MongoDB Testcontainer, spying on collections, document assertions. |

```kotlin
testImplementation("dev.invirt:invirt-test")
testImplementation("dev.invirt:invirt-security-test")
testImplementation("dev.invirt:invirt-mongodb-test")
```

See the [Web tests](/docs/api/invirt-test/web-tests), [Security tests](/docs/api/invirt-test/security-tests)
and [MongoDB tests](/docs/api/invirt-test/mongodb-tests) sections for the available helpers.
