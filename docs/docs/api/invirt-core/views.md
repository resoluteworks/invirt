---
sidebar_position: 9
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Views

See the [Views](/docs/framework/views-response) section for an overview.

### InvirtView
Base class for view models that associate a Pebble template with the rendering object.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    data class ListUsersResponse(
        val users: List<User>
    ) : InvirtView("users/list")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    open class InvirtView(val template: String)
    ```
  </TabItem>
</Tabs>

### InvirtView.ok / InvirtView.status
Renders an `InvirtView` with status 200 or a caller-supplied status.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    "/users" GET { request ->
        ListUsersResponse(users).ok(request)
    }

    "/users" POST { request ->
        CreateUserResponse(user).status(request, Status.ACCEPTED)
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun InvirtView.ok(request: Request): Response
    fun InvirtView.status(request: Request, status: Status): Response
    ```
  </TabItem>
</Tabs>

### renderTemplate
Renders a template directly, optionally with a model object, without a backing `InvirtView`.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    "/" GET { request -> renderTemplate(request, "index") }
    "/profile" GET { request -> renderTemplate(request, "profile", mapOf("name" to "John")) }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun renderTemplate(request: Request, template: String, model: Any? = null): Response
    ```
  </TabItem>
</Tabs>

### errorResponse
Renders a template with validation errors and an optional model. Returns
`Status.UNPROCESSABLE_ENTITY` by default.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    error { form, errors ->
        errorResponse(request, errors, "signup", form)
    }

    // One-off field errors without a backing model
    errorResponse(request, "signup", "email" to "Email already in use")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun errorResponse(
        request: Request,
        errors: ValidationErrors,
        template: String,
        model: Any? = null,
        status: Status = Status.UNPROCESSABLE_ENTITY
    ): Response

    fun errorResponse(
        request: Request,
        template: String,
        vararg errors: Pair<String, String>
    ): Response
    ```
  </TabItem>
</Tabs>

### InvirtView.asErrorResponse
Convenience that treats an `InvirtView` instance as both the model and the template name when rendering
an error response.
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun InvirtView.asErrorResponse(
        request: Request,
        errors: ValidationErrors,
        status: Status = Status.UNPROCESSABLE_ENTITY
    ): Response
    ```
  </TabItem>
</Tabs>
