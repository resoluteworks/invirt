---
sidebar_position: 2
---

# Web tests (invirt-test)

### postForm
Builds a `application/x-www-form-urlencoded` `POST` request with the given fields. Useful for driving
handlers that consume `request.toForm<T>()`.

```kotlin
val request = postForm("/signup", mapOf(
    "name" to "John",
    "email" to "john@example.com",
    "password" to "secret123"
))
val response = handler(request)
```

### Response assertions
All assertions are Kotest matchers and integrate with the standard `shouldBe` / `should` syntax.

```kotlin
response shouldBeRedirectTo "/signup/success"     // status 303 + Location header
response shouldHaveTemplate "signup"              // rendered via the named template
response shouldHaveCookieIgnoringExpiry someCookie

val (form, errors) = response.shouldBeErrorResponse<SignupForm>()
val errors = response.shouldBeErrorResponse()     // when no backing model is needed

val model = response.shouldHaveModel<MyResponseModel>()

response.shouldBeHtmlRedirectTo("/login")         // meta-refresh redirect from htmlRedirect()
```

`shouldHaveTemplate` and `shouldHaveModel` rely on Invirt attaching the
[`InvirtRenderModel`](/docs/framework/views-response) to the response context, so they work on any
response produced by Invirt's view helpers (`renderTemplate`, `InvirtView.ok`, `errorResponse`, ...).
