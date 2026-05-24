---
sidebar_position: 1
---

# Views

Invirt's view layer is a thin convention over http4k templating. A response that renders a Pebble template
is either produced from an `InvirtView` instance, or directly via `renderTemplate(request, "template")`.

## InvirtView

`InvirtView` is the base class for view models. It associates a model class with a template name so the
handler doesn't have to repeat the template path each time.

```kotlin
open class InvirtView(val template: String)
```

The `.peb` extension is optional in the template name. Both `InvirtView("users/list")` and
`InvirtView("users/list.peb")` resolve to the same template.

```kotlin
data class ListUsersResponse(
    val users: List<User>
) : InvirtView("users/list") // Points to webapp/views/users/list.peb

val handler = routes(
    "/users/list" GET { request ->
        ListUsersResponse(users).ok(request)
    },
    "/users/create" POST { request ->
        CreateUserResponse(user).status(request, Status.ACCEPTED)
    }
)
```

The `request` argument is required because Invirt makes the current `Request` available to the template
(see [Current request](/docs/framework/current-request)).

## Response helpers

| Function | Description |
|---|---|
| `InvirtView.ok(request)` | Renders the view with HTTP 200 |
| `InvirtView.status(request, status)` | Renders the view with the given status |
| `renderTemplate(request, template, model?)` | Renders an arbitrary template without an `InvirtView` |

```kotlin
"/" GET { request ->
    renderTemplate(request, "index")
}

"/profile" GET { request ->
    renderTemplate(request, "profile", mapOf("name" to "John"))
}
```

## Error responses

Validation errors are returned as a special kind of view response that exposes the failed `model` and
the `ValidationErrors` to the Pebble template (see [Form validation](/docs/framework/forms/form-validation)).

```kotlin
fun errorResponse(
    request: Request,
    errors: ValidationErrors,
    template: String,
    model: Any? = null,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response
```

```kotlin
"/signup" POST { request ->
    request.toForm<SignupForm>()
        .validate()
        .map {
            success { form -> httpSeeOther("/signup/success") }
            error { form, errors -> errorResponse(request, errors, "signup", form) }
        }
}
```

For convenience, an `InvirtView` can also be rendered as an error response directly:

```kotlin
SignupView().asErrorResponse(request, errors)
```

A short-form overload is available for one-off field errors without a backing model:

```kotlin
errorResponse(request, "signup", "email" to "Email already in use")
```
