---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# InvirtRequestContext

### request

Returns the [Request](https://www.http4k.org/api/org.http4k.core/-request/index.html) object for
the current HTTP transaction.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    println(InvirtRequestContext.request!!.uri)
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    val request: Request?
    ```
  </TabItem>
</Tabs>

### http4kRequestContexts

Invirt uses http4k's [RequestContexts](https://www.http4k.org/guide/howto/attach_context_to_a_request/) to
store various information about the request, including validation errors and authentication principal
in the [security module](/docs/framework/security/overview).

Please note that at the time of this writing, http4k's only supports one instance of `RequestContexts`
per application. This means that if you are using Invirt you cannot create your custom `RequestContexts` instance
and must use the one provided by Invirt.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    val http4kRequestContexts = RequestContexts()
    ```
  </TabItem>
</Tabs>

### validationErrors

Returns the validation errors for the current request. This is used internally by Invirt
to store validation errors and enable for them to be used in Pebble templates.

<Tabs>
    <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    val errors: ValidationErrors?
    ```
    </TabItem>
</Tabs>
