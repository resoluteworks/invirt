---
sidebar_position: 7
---

# Pebble Functions
A set of utility functions registered by Invirt that can be used inside Pebble templates.

### request()
Returns the current http4k `Request` (wrapped in an [InvirtRequest](/docs/framework/current-request#invirtrequest)).
This function is required inside macros, where the [context `request` object](/docs/api/invirt-core/pebble-context-objects#request)
is not visible. Outside macros either form works.

```html
{% macro requestSummary() %}
    {{ request().uri }}
    {{ request().method }}
{% endmacro %}

{{ request.method }}
{{ request.query("q") }}
```

### errors()
Returns the validation errors object (`io.validk.ValidationErrors`), or `null` if there are no validation
errors for the current request. Required inside macros &mdash; outside, the
[`errors`](/docs/api/invirt-core/pebble-context-objects#errors) context object is equivalent.

Read more about validation [here](/docs/framework/forms/form-validation).

```html
{% if errors().hasErrors("name") %}
    <div class="text-error">{{ errors().error("name") }}</div>
{% endif %}
```

### json(value)
Renders the given object as a JSON string. Typically used when embedding model data into a JavaScript
context.

```html
<div id="map" data-map-place='{{ json(model.place) | raw }}'></div>
```

### jsonArray(value)
Same as `json()`, but always renders the value as a JSON array. When `value` is not a collection it is
wrapped in a single-element array.

```html
<div id="map" data-map-places='{{ jsonArray(model.places) | raw }}'></div>
```

### today()
Returns the current date as a `java.time.LocalDate`.
```html
Today is {{ today() }}
```

### uuid()
Returns a new time-ordered UUIDv7 as a hex string (no dashes). Convenient for unique DOM ids in
templates.
```html
<input type="text" id="field-{{ uuid() }}"/>
```

### currencyFromMinorUnit(minorUnitAmount, currency)
Formats a minor-unit currency value (pence/cents) as a human-readable string using the currency's
symbol and default fraction digits.

```html
{{ currencyFromMinorUnit(order.totalMinorUnit, "GBP") }}  // £12.50
```

### pluralize(count, singular, plural)
Returns `singular` when `count == 1` and `plural` otherwise.

```html
You have {{ model.count }} {{ pluralize(model.count, "message", "messages") }}.
```

### dateWithDaySuffix filter
Formats a `java.time.Temporal` (`LocalDate`, `LocalDateTime`, `Instant`) using a `DateTimeFormatter`
pattern, inserting the English ordinal suffix on the day of month (`1st`, `2nd`, `3rd`, ...).

```html
{{ model.dispatchedAt | dateWithDaySuffix("EEEE, MMMM d yyyy") }}
{# → "Wednesday, March 1st 2026" #}
```
