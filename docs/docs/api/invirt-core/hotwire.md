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
