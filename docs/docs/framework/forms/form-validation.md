---
sidebar_position: 2
---

# Form validation

[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/form-validation)

## Design approach

Invirt's approach to form validation is based on general practices of how HTML forms
should work, and common UX practices in application development. Below are some of the constraints
and guiding principles for how we've implemented this on top of http4k.

#### Explicit, server-side validation
Validation is an explicit step performed by the http4k handler, there's no "magic" and no annotations
(nothing implicit, that is).
Based on the validation result, the handler has to make an explicit decision about the outcome:
respond with the success state or re-render the form to allow the user to correct the input.

#### Retaining input is (often) important
It's often that an application needs to retain the incorrectly entered values after a validation failure.
This is essential in making sure the user doesn't have to re-key all the inputs.

When validation is performed server side, the approach is slightly different to a single page application.
The handler needs to re-render the form on a validation error _and_ present the previously entered values.
Invirt tries to make this process as frictionless as possible, but also gives the developer plenty of latitude
for customisation.

In summary, at a high level, this is what Invirt is going for.

![Validation](assets/form-validation-concept.png)

## Validation framework
Invirt uses the [validk](https://github.com/resoluteworks/validk) Kotlin validation framework which your
application must wire as a dependency and use in its handlers to validate form inputs.
