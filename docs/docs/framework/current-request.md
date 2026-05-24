---
sidebar_position: 3
---

# Current HTTP Request

Invirt exposes the current http4k `Request` to the Pebble template engine, so templates can
read query parameters, build new URIs, or render request-derived state without the handler having to
pass each value explicitly.

The current request is made available automatically whenever you respond with a view via
`InvirtView.ok(request)`, `InvirtView.status(request, status)`, `renderTemplate(request, ...)`,
or `errorResponse(request, ...)`.

## In Pebble templates
Pebble templates can access the request directly via the [`request`](/docs/api/invirt-core/pebble-context-objects#request)
object in the root context, when not inside a macro. Inside macros, use the [`request()`](/docs/api/invirt-core/pebble-functions#request)
function instead, since [macros don't have access to the global context](https://pebbletemplates.io/wiki/tag/macro/).

```html
{% macro requestSummary() %}
    {{ request().uri }}
    {{ request().method }}
{% endmacro %}

{{ request.method }}
{{ request.query("q") }}
```

## InvirtRequest
Inside templates the request is wrapped in an `InvirtRequest`, which implements http4k's
[`Request`](https://www.http4k.org/api/org.http4k.core/-request/) interface by delegating to the original
request. The wrapper adds Kotlin-extension-style helpers that Pebble cannot otherwise reach (Pebble does not
resolve Kotlin extension functions), for [URI manipulation](/docs/api/invirt-core/uri-extensions) and
[sort handling](/docs/api/invirt-data/sort#sort-in-query-parameters).

```html
{% if request.hasQueryValue('type', 'person') %}
    <a href="{{ request.replaceQuery('type', 'company') }}">
        Show companies
    </a>
{% endif %}
```

## Access from a handler
Inside a handler the request is available as the lambda argument, so there is no need for a thread-local
or context lookup.

```kotlin
"/items" GET { request ->
    val sort = request.sort()
    val page = request.page()
    // ...
}
```
