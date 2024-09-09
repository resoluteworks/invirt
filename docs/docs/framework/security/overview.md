---
sidebar_position: 1
---

import happyFlow from './assets/authentication-happy-flow.png';

# Overview

Invirt's security module focuses exclusively on authentication and it provides
a set of components for transparently authenticating HTTP requests via a custom [http4k filter](https://www.http4k.org/guide/reference/core/#filters).

## Dependency
```kotlin
implementation(platform("dev.invirt:invirt-bom:${invirtVersion}"))
implementation("dev.invirt:invirt-security")
```

## Use case
The first problem we aim to solve is to provide an application with context for the currently
authenticated user (Principal) transparently, and allowing for that to be checked anywhere within the stack.
We use request context and a ThreadLocal for this purpose.

Second, we want to allow the application to decide what authentication solution it wants to use, with Invirt simply
providing the scaffolding to wire that in, and secure certain application routes.

Lastly, we wanted to make the tooling as un-intrusive as possible, and allow the application to define
the concepts of user or principal according to its requirements, without heavy constraints from the framework
on how these must be implemented and handled.

Below is a high level view of a happy flow for authenticating a request using cookies.
We discuss [these concepts](/docs/framework/security/core-concepts) in detail and
there is also an [example application](/docs/framework/security/example) to explore them.

<img src={happyFlow}/>

## What Invirt Security doesn't do

#### Login/Logout
As these operations are usually heavily coupled to the authentication provider being used and the application
design, we left this to the developer to wire according to the system requirements. Handling magic links, MFA
and other authentication options is not something that Invirt wants to or can prescribe.

#### Authorisation
Invirt doesn't implement authorisation semantics, as we felt that this is an area where the application
must be allowed flexibility. We didn't want to make any assumptions about the applications authorisation requirements
and whether, for example, it should use RBAC (Role Based Access Control) or ABAC (Attribute-Based Access).

However, if you're interested in a lightweight RBAC library for Kotlin there's [Klees](https://github.com/resoluteworks/klees)
which is owned by the Invirt maintainer.

#### Path-based access control
Some MVC frameworks provide utilities to define URI paths and regular expressions to secure certain routes and
resources based on a Principal's role or attributes. For example `/admin/*` can only be accessed by Role.ADMIN, etc.
This is a practice that has a lot of limitations and it leads to a code base that is hard to maintain.
It also falls in the realm of authorisation, which we discarded above.

That being said, should you require something along these lines, Invirt provides a basic utility to wire
custom Principal checks via a filter, but using a functional style.
```kotlin
val permissionChecker: (Principal) -> Boolean = { principal ->
    "ADMIN" in principal.roles
}

val handler = securedRoutes(
    permissionChecker,
    routes(
        "/admin" GET { Response(Status.OK) },
        "/admin/test" GET { Response(Status.OK) }
    )
)
```

More commonly though, at this level, you'd want to secure certain routes from being accessed
without a Principal present on the request. This can be done with `authenticatedRoutes()`.

```kotlin
val handler = authenticatedRoutes(
    DashboardHandler(),
    LogoutHandler()
)
```
