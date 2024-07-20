---
sidebar_position: 1
---

# Template Context Objects
These objects are available in the root context when rendering a Pebble template.

### model
The model (object) for the template being rendered. Invirt uses the same naming as
[http4k model](https://www.http4k.org/guide/reference/templating/#notes_for_pebble) for this.

```html
<input type="text" name="firstName" value="{{ model.firstName }}"/>
```

### request
Reference to the current http4k `Request` object. See [Current request in Pebble templates](/docs/framework/current-request#current-request-in-pebble-templates)
for more details.

```html
{{ request.method }}
{{ request.query("q") }}
```

### errors
Validation errors (`io.validk.ValidationErrors`) for the current request, or `null` if there are no validation
errors. Read more about validation [here](/docs/framework/forms/form-validation).

```html
{% if errors.hasErrors("name") %}
    <div class="text-error">{{ errors.error("name") }}</div>
{% endif %}
```
