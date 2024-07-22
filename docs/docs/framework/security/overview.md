---
sidebar_position: 1
---

import happyFlow from './assets/authentication-happy-flow.png';

# Overview

Invirt's security module focuses exclusively on authentication and it provides
a set of components for transparently authenticating HTTP requests via a custom [http4k filter](https://www.http4k.org/guide/reference/core/#filters).

## Dependency
```kotlin
implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
implementation("io.resoluteworks:invirt-security")
```

## Use case
The first problem we aim to solve is to provide an application with context for the currently
authenticated user (Principal) transparently, and allowing for that to be checked anywhere within the stack.
We use a ThreadLocal for this purpose, and request context.

Second, we want to allow the application to decide what authentication solution it wants to use, with Invirt simply
providing the scaffolding to wire that in, and secure certain application routes.

Lastly, we wanted to make the tooling as un-intrusive as possible, and allow the application to define
the concepts of user or principal according to its requirements, without heavy constraints from the framework
on how these must be implemented and handled.

Below is a high level view of a happy flow for authenticating a request using request cookies.

<img src={happyFlow}/>

## What Invirt Security doesn't do

#### Login/Logout
As these operations are usually heavy coupled to the authentication provider being used and the application
design, we left this to the developer to wire according to the system requirements.

#### Authorisation
Invirt doesn't implement authorisation semantics, as we felt that this is an area where the application
must be allowed flexibility. We didn't want to make any assumptions about the applications authorisation requirements
and whether it should use RBAC (Role Based Access Control) or ABAC (Attribute-Based Access), for example.

#### Path-based access control
Some frameworks provide utilities to define paths and regular expressions to secure certain routes and
resources based on a Principal's role or attributes. For example `/admin/*` can only be accessed by Role.ADMIN.
This is a practice that has a lot of limitations which we wanted to avoid. It also falls in the realm
of authorisation, which we discussed above.

That being said, Invirt provides a basic utility to wire custom authorisation checks via a filter, allowing the application
to implement custom checks for specific application routes and resources in a functional style.
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

