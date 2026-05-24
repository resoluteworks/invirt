---
sidebar_position: 6
---

# Pebble Context Objects
These objects are available in the root context when rendering a Pebble template via Invirt's
view helpers ([`renderTemplate`](/docs/api/invirt-core/views#rendertemplate),
[`InvirtView.ok`](/docs/api/invirt-core/views#invirtviewok--invirtviewstatus), etc).

### model
The model object passed to the renderer. Invirt uses the same naming as the
[http4k Pebble integration](https://www.http4k.org/guide/reference/templating/#notes_for_pebble).

```html
<input type="text" name="firstName" value="{{ model.firstName }}"/>
```

### request
Reference to the current http4k `Request` (wrapped in an [`InvirtRequest`](/docs/framework/current-request#invirtrequest)).
See [Current request](/docs/framework/current-request#in-pebble-templates) for the available helpers.

```html
{{ request.method }}
{{ request.query("q") }}
```

### errors
Validation errors (`io.validk.ValidationErrors`) for the current request, or `null` if there are none.
Read more about validation [here](/docs/framework/forms/form-validation).

```html
{% if errors.hasErrors("name") %}
    <div class="text-error">{{ errors.error("name") }}</div>
{% endif %}
```

### Context variables from `InvirtPebbleConfig`
Any per-request values registered via
[`InvirtPebbleConfig.contextVariables`](/docs/framework/configuration#pebble-configuration) appear here
under the names supplied in the configuration map.
