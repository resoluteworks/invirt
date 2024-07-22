---
sidebar_position: 1
---

# Overview

Invirt's security module focuses exclusively on authentication (not authorisation) and it provides
a set of components for transparently authenticating HTTP requests via custom [http4k filters](https://www.http4k.org/guide/reference/core/#filters).

## Dependency
```kotlin
implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
implementation("io.resoluteworks:invirt-security")
```

## Use case
The first problem we aim to solve with this module is to provide an application with context for the currently
authenticated user transparently, and allowing for that to be checked anywhere within the stack.

Second, we want to allow the application to decide what authentication solution it wants to use, and simply
provide the scaffolding to wire that in and secure the application's routes.

Lastly, we wanted to make the tooling as un-intrusive as possible, and allow the application to define
the concepts of user or principal according to its requirements, without the heavy constraints from the framework
on how these must be implemented.

In other words, we wanted to provide a way to easily reason about a browser's authentication context (like cookies)
and the system's requirements to secure certain routes against those credentials.

## Core concepts

### Principal
Invirt's definition of Principal is similar to the one in other MVC and server frameworks, in the sense
that it refers to the currently authenticated entity or user operating the system.

An essential difference, however, is that Invirt doesn't have a native concept of "anonymous"
Principal (i.e. not authenticated user) as we felt this would add a layer of complexity that doesn't
benefit the framework's objectives. That being said, it shouldn't be hard for an application to handle this should it require to.

In Invirt, the Principal object is defined as a marker interface with no properties or functions, and only
a companion object with some utilities for exposing the entity to the rest of the application.

```kotlin
interface Principal {

    companion object {
        val current: Principal ...
        ...
    }
}
```

An Invirt application must implement this interface in a "user" class to define its properties and behaviour
and to allow that component to interact with the rest of the Invirt Security mini-framework. Again, we didn't
want to prescribe the attributes and features of a Principal, and we leave it to the application to do so.

## Authentication
This component defines
