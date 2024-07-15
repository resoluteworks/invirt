---
sidebar_position: 3
---

# Request object
Invirt provides several mechanisms for the application to access the current http4k request object outside the
handler. This is useful for several scenarios, including template rendering that requires context about
the current HTTP request.

Invirt also provides a series of utilities for operating on the current request object, including manipulating
the URI or query parameters, which can be employed for use cases like toggling filters or resetting query parameters.

## Kotlin code
Invirt uses an http4k filter to store the current http4k request in a thread local and clear it after the
request completes. This is done automatically when wiring [InvirtRequestContext](/docs/framework/invirt-context).
`InvirtRequestContext` then exposes a `currentRequest` property which can be accessed anywhere within the application.
```kotlin
println(InvirtRequestContext.currentRequest!!.uri)
```

## Current request in Pebble templates
Pebble templates can access the request object either directly from the [`request`](/docs/api/pebble/pebble-context-objects#request)
object in the root context, when not inside a macro. Inside macros, the [`request()`](/docs/api/pebble/pebble-functions#request)
Pebble function must be used instead (due to the fact that [macros don't have access to the global context](https://pebbletemplates.io/wiki/tag/macro/)).

```html
{% macro requestSummary() %}
    {{ request().uri }}
    {{ request().method }}
{% endmacro %}

{{ request.method }}
{{ request.query("q") }}
```

## URI extension functions
Invirt provides a set of extensions to enable the manipulation of a request's URI. Below are a few examples, and
the complete list can be found [here](https://github.com/resoluteworks/invirt/blob/main/invirt-core/src/main/kotlin/invirt/http4k/uri.kt).

```kotlin
fun Uri.hasQueryValue(name: String, value: String): Boolean { ... }

fun Uri.removeQueryValue(name: String, value: Any): Uri { ... }

fun Uri.toggleQueryValue(name: String, value: Any): Uri { ... }

fun Uri.removeQueries(names: Collection<String>): Uri { ... }
```

## InvirtRequest
Invirt wraps the core http4k `Request` object in an `InvirtRequest`, which implements http4k's [Request interface](https://www.http4k.org/api/org.http4k.core/-request/)
and simply delegates to the underlying `Request` object.

```kotlin
class InvirtRequest(val delegate: Request) : Request by delegate {

    fun hasQueryValue(name: String, value: String): Boolean = delegate.uri.hasQueryValue(name, value)
    fun toggleQueryValue(name: String, value: Any): Uri = delegate.uri.toggleQueryValue(name, value)
    fun replacePage(page: Page): Uri = delegate.uri.replacePage(page)
    fun replaceQuery(name: String, value: Any): Uri = delegate.uri.replaceQuery(name to value)
    ...
}
```

Within the Kotlin code you don't typically need access to the `InvirtRequest` object, as the functionality it defines
is already contained in Kotlin extensions, as discussed above. However, within the context of a Pebble template,
we require this object to get access to these extensions, as Pebble templates can't access Kotlin extension functions
directly.

```html
{% if request.hasQueryValue('type', 'person') %}
    <a href="{{ request.replaceQuery('type', 'company') }}">Show companies</a>
{% endif %}
```
