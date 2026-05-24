---
sidebar_position: 2
---

# Core concepts

## Principal
Invirt's definition of `Principal` is similar to the one in other MVC and server frameworks, in the sense
that it refers to the currently authenticated entity or user operating the application.

An important difference is that Invirt doesn't have a native concept of an "anonymous"
Principal (i.e. an unauthenticated user). We felt this would add a layer of complexity that doesn't
benefit the framework's objectives. It shouldn't be hard for an application to handle this should it need to.

In Invirt, the `Principal` interface only requires a `ref` property &mdash; a stable, lightweight reference
to the authenticated entity (its type and unique identifier). It is intended for retrieving the full principal
details from a data source when needed, and for use in logs and audit trails.

```kotlin
interface Principal {
    val ref: PrincipalRef
}

data class PrincipalRef(val type: String, val id: String) {
    override fun toString(): String = "$type:$id"
}
```

An Invirt application implements this interface in a "user" class to define its properties and behaviour
and allow it to interact with the rest of Invirt Security. The framework doesn't prescribe additional
attributes on a `Principal` &mdash; that is left to the application.

```kotlin
data class User(
    val id: String,
    val email: String,
    val roles: Set<String>
) : Principal {
    override val ref = PrincipalRef("user", id)
}
```

### Accessing the current Principal
The current `Principal` is attached to the http4k `Request` via http4k's
[request context](https://www.http4k.org/guide/howto/attach_context_to_a_request/). The following
extensions are provided:

```kotlin
val Request.principal: Principal?      // returns the principal or null
val Request.hasPrincipal: Boolean      // true when a principal is attached
fun Request.withPrincipal(p: Principal): Request   // attaches a principal
```

```kotlin
"/me" GET { request ->
    val user = request.principal as? User
    // ...
}
```

There is no thread-local lookup &mdash; the principal travels with the request.

## Authenticator
This component defines the logic to authenticate an HTTP request. It is a functional interface
with a single method that the application must implement.

```kotlin
fun interface Authenticator {
    fun authenticate(request: Request): AuthenticationResponse
}
```

`Authenticator` is plugged into `AuthenticationFilter` (see below). Its `authenticate()` function
must respond with an `AuthenticationResponse` indicating whether a Principal could be authenticated from the
request's properties (typically cookies or headers).

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
`AuthenticationFilter` is the Invirt component responsible for calling `Authenticator` above and storing
the principal (if authentication is successful) on the current `Request`.

When `AuthenticationResponse.Authenticated` contains a non-empty `newCookies`, `AuthenticationFilter`
will set these on the response once the request completes. This is useful for login scenarios when
we need to set the initial cookies, but also for refreshing authentication credentials stored in cookies (like JWT tokens).

It's important to note that `AuthenticationFilter` doesn't act as a gateway to prevent requests from proceeding
when a principal isn't present, or when the principal doesn't match the criteria to access the resource.
It's within the remit of the application to handle that, as it falls within the realm of
[authorisation](/docs/framework/security/overview#authorisation), not authentication.

The filter's responsibility is simply to extract a `Principal` object from the request, via the `Authenticator`
component, attach it to the request, and forward to the underlying handler.

```kotlin
val appHandler = AuthenticationFilter(authenticator)
    .then(routes(/* ... */))
```
