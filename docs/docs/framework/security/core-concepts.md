---
sidebar_position: 2
---

# Core concepts

## Principal
Invirt's definition of Principal is similar to the one in other MVC and server frameworks, in the sense
that it refers to the currently authenticated entity or user operating the application.

An essential difference, however, is that Invirt doesn't have a native concept of "anonymous"
Principal (i.e. not authenticated user) as we felt this would add a layer of complexity that doesn't
benefit the framework's objectives. That being said, it shouldn't be hard for an application to handle this should it require to.

In Invirt, the Principal object is defined as a marker interface with no properties or functions, and only
a companion object with some utilities for exposing the entity to the rest of the application.

```kotlin
interface Principal {

    companion object {
        /**
         * Returns the [Principal] on the current thread if present, `null` otherwise
         */
        val currentSafe: Principal? get() = principalThreadLocal.get()

        /**
         * Returns the [Principal] on the current thread if present, fails otherwise
         */
        val current: Principal get() = currentSafe ?: throw IllegalStateException("No Principal found on current threads")

        /**
         * Checks if a [Principal] is present on the current thread.
         */
        val isPresent: Boolean get() = principalThreadLocal.get() != null
    }
}
```

An Invirt application will implement this interface in a "user" class to define its properties and behaviour
and allow it to interact with the rest of the Invirt Security mini-framework. Again, we didn't
want to prescribe the attributes and features of a Principal, and we leave it to the application to do so.

## Authenticator
This component defines the logic to authenticate an HTTP request and is an interface with a single method
that the application must implement.

```kotlin
interface Authenticator {
    fun authenticate(request: Request): AuthenticationResponse
}
```

This interface is typically wired in the `AuthenticationFilter` discussed below, and its `authenticate()` function
must respond with an `AuthenticationResponse` indicating whether a Principal could be authenticated from the
Request's properties (typically cookies).

```kotlin
sealed class AuthenticationResponse {

    data object Unauthenticated : AuthenticationResponse()

    data class Authenticated<P : Principal>(
        val principal: P,
        val newCookies: List<Cookie> = emptyList()
    ) : AuthenticationResponse()
}
```

## AuthenticationFilter
The AuthenticationFilter is the main component responsible for calling `Authenticator` above and storing
the principal (if authentication is successful) on the current Request (context), and on a ThreadLocal.

When `AuthenticationResponse.Authenticated` contains a non-empty `newCookies`, `AuthenticationFilter`
will set these on the response, once the request completes. This is useful for both login scenarios, but also
for refreshing authentication credentials stored in cookies (like JWT tokens).
