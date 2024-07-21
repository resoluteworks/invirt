---
sidebar_position: 2
---

# Pebble Functions
A set of utility functions that can be used by Pebble templates in an Invirt application.

### request()
Returns the current http4k `Request` object (as an [InvirtRequest](/docs/framework/current-request)).
The function is only required in the context of a macro - but can be used
anywhere. Outside macros, the [context request object](/docs/api/invirt-core/pebble/pebble-context-objects#request) can be used directly.

```html
{% macro requestSummary() %}
    {{ request().uri }}
    {{ request().method }}
{% endmacro %}

{{ request.method }}
{{ request.query("q") }}
```

### errors()
Returns the validation errors object (`io.validk.ValidationErrors`) or `null` if there are no validation
errors for the current request. Read more about validation [here](/docs/framework/forms/form-validation).

```html
{% if errors().hasErrors("name") %}
    <div class="text-error">{{ errors().error("name") }}</div>
{% endif %}
```

### json(object)
Renders the specified model object as a JSON string. Typically used when you need to render a JSON
object consumed via JavaScript.

```html
<div id="map" data-map-place='{{ json(model.place) | raw }}'></div>
```

### jsonArray(collection | object)
Similar to the `json` function, except it renders the argument as a JSON array. When the argument
is not a collection a one-element JSON array is rendered.

```html
<div id="map" data-map-places='{{ jsonArray(model.places) | raw }}'></div>
```

### today()
Returns the current date as a `java.time.LocalDate`
```html
Today is {{ today() }}
```
