---
sidebar_position: 3
---

# Security tests (invirt-security-test)

### Authenticator test doubles
Two ready-to-use `Authenticator` implementations for unit-testing handlers that depend on
`AuthenticationFilter`.

```kotlin
// Always returns Unauthenticated
val auth = failingAuthenticator

// Always returns Authenticated(principal), optionally with newCookies
val auth = successAuthenticator(myPrincipal)
val auth = successAuthenticator(myPrincipal, newCookies = listOf(Cookie("session", token)))
```

Compose them into a real handler chain via `AuthenticationFilter(auth).then(routes)`.

### AuthenticationResponse assertions
```kotlin
val response: AuthenticationResponse = authenticator.authenticate(request)

val authed: AuthenticationResponse.Authenticated<MyPrincipal> = response.shouldBeSuccessful()
authed.principal // MyPrincipal

response.shouldNotBeSuccessful() // asserts AuthenticationResponse.Unauthenticated
```
