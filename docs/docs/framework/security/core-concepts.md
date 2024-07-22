---
sidebar_position: 2
---

# Core concepts

## Principal
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
