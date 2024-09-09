---
sidebar_position: 3
---

# Current HTTP Request
Invirt provides several mechanisms for the application to access the current http4k `Request` object outside the
handler. This is useful for several scenarios, including the rendering of templates that require access to the
current request or URI.

## In Kotlin
Invirt automatically stores the current http4k request in the [InvirtRequestContext](/docs/api/invirt-core/request-context),
which in turn exposes a readonly `request` property that can be used to access the request anywhere within the application.
```kotlin
println(InvirtRequestContext.request!!.uri)
```

## In Pebble templates
Pebble templates can access the request object directly from the [`request`](/docs/api/invirt-core/pebble-context-objects#request)
object in the root context, when not inside a macro. Inside macros, the [`request()`](/docs/api/invirt-core/pebble-functions#request)
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
[URI extensions](/docs/api/invirt-core/uri-extensions).

You don't typically need access to the `InvirtRequest` object from your applications Kotlin code. However, in a Pebble template,
this is required in order to provide access to above-mentioned extensions, as Pebble can't operate Kotlin extension functions directly.

```html
{% if request.hasQueryValue('type', 'person') %}
    <a href="{{ request.replaceQuery('type', 'company') }}">
        Show companies
    </a>
{% endif %}
```
