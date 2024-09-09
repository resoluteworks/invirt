---
sidebar_position: 2
---

import validationConcept from './assets/form-validation-concept.png';
import formSignupScreen from './assets/form-validation-signup-screen.png';

# Form validation

[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/form-validation)

## Design approach

Invirt's approach to form validation is based on general practices of how HTML forms
should work, and common UX principles. Below are some of the constraints
and guiding principles for how Invirt implements these on top of http4k.

#### Explicit, server-side validation
Validation is an explicit step performed by the http4k handler, there's no "magic" and no annotations.
Based on the validation result, the handler has to make an explicit decision about the outcome:
respond with the success state or re-render the form to allow the user to correct the input.

#### Retaining input is (often) important
It's common for an application to need to retain the incorrectly entered values after a validation failure.
This is essential in making sure the user doesn't have to re-key all the inputs.

When validation is performed server side, the approach is different to a single page application.
The handler needs to re-render the form on a validation error _and_ present the previously entered values.
Invirt tries to make this process as frictionless as possible, but also gives the developer plenty of latitude
for customisation.

In summary, at a high level, this is what Invirt is going for.

<img src={validationConcept} width="800"/>

## Validation framework
Invirt uses the [Validk](https://github.com/resoluteworks/validk) Kotlin validation framework which your
application must wire as a dependency and use in its handlers to validate form inputs.

```kotlin
implementation "io.resoluteworks:validk:${validkVersion}"
```
Validk is not part of the Invirt framework as it's been designed as a stand-alone re-usable
library. But it's under the tutelage of the same maintainers as Invirt.

## Submission flow
Below is a very basic example of a form collecting signup details for a user. The form
validation has the following requirements:
* Name is required and must be at least 5 characters long.
* Email is required and must be a valid email address.
* Password is required and must be at least 8 characters long.

We want to display the relevant validation error messages for each field, but we also
want to present a "Please correct the errors below" message at the top, when the input doesn't
pass validation.

<img src={formSignupScreen} width="800"/>

<br/>
<br/>

The right side of the image depicts the desired outcome of a form submission that returns
validation errors. A key element here is that we want to return the previously entered value for
Name and Email, to avoid the user having to re-key these, but we don't want to send back a
previously entered invalid password.

### Form and model
Below are the (stripped down) HTML form and respective Kotlin object for handling the form above.

```html
<form action="/signup" method="POST">
    <input type="text" name="name"/>
    <input type="text" name="email"/>
    <input type="password" name="password"/>
    <button type="submit">Sign up</button>
</form>
```

```kotlin
data class SignupForm(
    val name: String,
    val email: String,
    val password: String,
) : ValidObject<SignupForm> {

    override val validation = Validation {
        ...
    }
}
```

### Handling form validation
Below is the handler that would then read this form, validate it, and return a response based on this outcome,
including error messages and previously entered input values.

```kotlin
"/signup" POST { request ->
    request.toForm<SignupForm>()
        .validate {
            error { form, errors ->
                errorResponse(form, errors, "signup.peb")
            }
            success { form ->
                // Signup user with the date on the form and redirect to /signup/success
                httpSeeOther("/signup/success")
            }
        }
}
```

But there are several steps here, so let's take it one at a time. Firstly, we read the form
into a `SignupForm` object, with the construct discussed in the [previous section](/docs/framework/forms/form-basics).
```kotlin
request.toForm<SignupForm>()
```

Because `SignupForm` implements Validk's `ValidObject` interface it means we can call
`.validate` on this directly, which in turn allows us to provide custom handling
logic for success and failure scenarios.

In both cases, we want to return an http4k `Response`. For the success scenario
we simply return an HTTP 303 using Invirt's `httpSeeOther` utility.

For the error scenario, we want to return a view response, which renders the form again via
`signup.peb`, the template we used to render the initial (empty) form.

The error scenario uses Invirt's `errorResponse` utility which produces an http4k view response
with a special implementation of the [ViewModel](https://www.http4k.org/api/org.http4k.template/-view-model/).

```kotlin
internal class ErrorResponseView(
    val model: Any,
    val errors: ValidationErrors,
    val template: String
) : ViewModel {
    override fun template() = template
}
```

When returning `errorResponse(form, errors, "signup.peb")`, Invirt's custom Pebble rendering detects
that we're trying to render an error response and exposes the passed `errors` argument into the
template context, and the form as the `model`. You can read more about accessing errors in your template
[here](/docs/api/invirt-core/pebble-context-objects#errors).

### Displaying error messages

There are then two things we can add to our HTML form. First, we can set a value for our inputs
to display a previously entered value. Second, we can show an error message for each input
by checking if the field has any errors. This is a common pattern that you've likely encountered
in other MVC frameworks.

To omit the password value from being rendered back on the form, we simply don't
add a `value` to the password input, essentially leaving it as per earlier definition.

```html
<input type="text" name="name" value="{{ model.name }}"/>

{% if errors.hasErrors("name") %}
    <div class="text-error">{{ errors.error("name") }}</div>
{% endif %}

...

<input type="password" name="password"/>

{% if errors.hasErrors("password") %}
    <div class="text-error">{{ errors.error("password") }}</div>
{% endif %}
```

Lastly, we can also check for the presence of validation errors and display the custom message
at the top of the form.
```html
{% if errors != null %}
    <div class="text-lg font-semibold text-error">Please correct the errors below</div>
{% endif %}
```

### Accessing errors from a Pebble macro
[Pebble macros](https://pebbletemplates.io/wiki/tag/macro/) don't have the context of the view being rendered,
so the `errors` object above (or `model`, for that matter) won't be accessible from a macro. When you need to handle
validation errors within a macro, simply use the `error()` function instead.
```html
{% if errors() != null %}
    <div class="text-lg font-semibold text-error">Please correct the errors below</div>
{% endif %}
...
{% if errors().hasErrors("password") %}
    <div>{{ errors().error("password") }}</div>
{% endif %}
```

## A note on error messages
By default, the Validk framework stops after the first validation error for a field, and returns
only that error message for it (fail-fast). This can be turned off
so it returns the complete list of failures for a field, when that's required.
You can read more about this [here](https://github.com/resoluteworks/validk?tab=readme-ov-file#fail-fast-validation).
