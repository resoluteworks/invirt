---
sidebar_position: 3
---

# Current HTTP request
Invirt provides several mechanisms for the application to access the current http4k `Request` object outside the
handler. This is useful for several scenarios, including the rendering of templates that require access to the
current request or URI.

## In Kotlin
Invirt uses an http4k filter to store the current `Request` in a thread local and clear it after the
request completes. This is done automatically when wiring
[InvirtRequestContext](http://localhost:3000/docs/framework/quickstart#2-wiring-the-invirtrequestcontext-filter).
`InvirtRequestContext` then exposes a `currentRequest` property which can be accessed anywhere within the application.
```kotlin
println(InvirtRequestContext.currentRequest!!.uri)
```

## In Pebble templates
Pebble templates can access the request object directly from the [`request`](/docs/api/pebble/pebble-context-objects#request)
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

## InvirtRequest
Invirt wraps the core http4k `Request` object in an `InvirtRequest`, which implements http4k's [Request interface](https://www.http4k.org/api/org.http4k.core/-request/).
`InvirtRequest` delegates to the native http4k `Request` for all interface operations, and adds a set of functions for wiring
[URI extension](/docs/api/kotlin/uri-extensions) to allow them to be used in Pebble templates.

Within the Kotlin code you don't typically need access to the `InvirtRequest` object. However, in a Pebble template,
we require this type in order to provide access to above-mentioned extensions, as Pebble can't operate Kotlin extension functions directly.

```html
{% if request.hasQueryValue('type', 'person') %}
    <a href="{{ request.replaceQuery('type', 'company') }}">
        Show companies
    </a>
{% endif %}
```
