---
sidebar_position: 11
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Hotwire

Small helpers for working with [Hotwire Turbo](https://turbo.hotwired.dev/) streams.

### Response.turboStream
Marks an existing response as a Turbo Stream by replacing its `Content-Type` with
`text/vnd.turbo-stream.html`.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    renderTemplate(request, "items/_row", model).turboStream()
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Response.turboStream(): Response
    ```
  </TabItem>
</Tabs>

### turboStreamRefresh
Returns a Turbo Stream response with a `<turbo-stream action="refresh">` body, used to ask the client
to perform a full page refresh.
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun turboStreamRefresh(): Response
    ```
  </TabItem>
</Tabs>

### turboStreamRedirect
Returns a Turbo Stream response with a `<turbo-stream action="redirect">` body, used to navigate the
browser to another page. Turbo swaps the body of a stream response into the current page rather than
following a redirect, so a handler answering a Turbo form submission sends this instead of a `303` when the
next thing the user should see is another page.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    turboStreamRedirect("/items/${item.id}")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun turboStreamRedirect(url: String): Response
    ```
  </TabItem>
</Tabs>
