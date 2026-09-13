---
sidebar_position: 4
---

# Filters

These are a set of utility filters that come with Invirt and which you can opt for in your application.

## CatchAll

Logs all exceptions and maps exception types to HTTP response statuses.
When an exception is caught that isn't mapped, a `Status.INTERNAL_SERVER_ERROR` is returned.

```kotlin
val handler = CatchAll(
    ValidationException::class to Status.BAD_REQUEST,
    AuthorisationException::class to Status.NOT_FOUND
).then(routes(/* ... */))
```

## ErrorPages
Automatically renders a Pebble template for specified HTTP error statuses.
The HTTP status of the underlying response is preserved in the final response.

```kotlin
// Renders the template "error/404.peb"
val httpHandler = ErrorPages(Status.NOT_FOUND to "error/404")
    .then(routes(/* ... */))
```

## StatusOverride
Overrides HTTP response status codes. The example below combines `StatusOverride` and `ErrorPages` to render
a "page not found" response when a user attempts to access a secured resource.

```kotlin
// securityFilter returns a Status.FORBIDDEN when a user
// tries to access a secure resource/page
ErrorPages(Status.NOT_FOUND to "error/404")
    .then(StatusOverride(Status.FORBIDDEN to Status.NOT_FOUND))
    .then(securityFilter)
    .then(routes)
```

## HeadAsGet
Answers a HEAD request by running the handler as GET and discarding the response body. http4k's routing
matches the request method by strict equality, so a route bound only to GET otherwise answers HEAD with a
`Status.METHOD_NOT_ALLOWED` - which trips up uptime monitors and link-preview services, since they check that a
page is alive with a HEAD request rather than a GET.

`HeadAsGet` wraps a handler instead of being a `Filter`: `Filter.then(RoutingHttpHandler)` applies a filter to
each route only after the method has been matched, which is too late to rewrite HEAD. Compose filters that must
see the original method (an access log) outside it, and filters whose bodies must be dropped for HEAD (error
pages) inside it:

```kotlin
HttpAccessLog()
    .then(
        HeadAsGet(
            ErrorPages(Status.NOT_FOUND to "error/404")
                .then(routes(/* ... */))
        )
    )
```

## HttpAccessLog
Logs HTTP transactions through [`kotlin-logging`](https://github.com/oshai/kotlin-logging). By default the
filter logs:

* Only HTTP transactions with error response statuses (4xx, 5xx)
* All routes/paths
* All headers

This can be configured to log all HTTP transactions, ignore certain URI paths or exclude headers from being logged.
You can also attach extra fields by providing an `extraFields` lambda
with an [`HttpTransaction`](https://www.http4k.org/api/org.http4k.core/-http-transaction/) argument and returning
a `Map<String, String>`.

```kotlin
val handler = HttpAccessLog(
    allStatues = true,
    ignorePaths = setOf("/admin"),
    excludeHeaders = setOf("jwt-token"),
    extraFields = { httpTransaction ->
        mapOf("remoteIp" to (httpTransaction.request.header("X-Forwarded-For") ?: ""))
    }
).then(routes)
```

## Caching
`cacheDays(days)` and `cacheOneYear()` are thin wrappers over http4k's caching filter, primarily intended
for the [static assets](/docs/framework/static-assets) routes.

```kotlin
"/static/${assetsVersion}" bind cacheDays(365).then(staticAssets(developmentMode))
```
