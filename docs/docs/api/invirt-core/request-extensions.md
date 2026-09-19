---
sidebar_position: 5
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Request extensions

### Request.pathOrNull()
Returns the value of a path variable of the route this request matched, or `null` when there is none to
read. Use it from a filter wrapping a whole route group, which runs before the leaf route binds its path
lens: http4k attaches the uri template outside the group's filters, so the variable is already readable
there.

Null covers every way the answer can be absent. The same filter also sees the requests that matched no
route (a 404) or matched the path with the wrong method (a 405), and those carry no uri template at all;
http4k's own `Request.path(name)` throws on them, which would turn a 404 into a 500. A value that matched
nothing but whitespace is null too.

<Tabs>
<TabItem value="example" label="Example" default>
    ```kotlin
    val suspendedGate = Filter { next ->
        { request ->
            val projectId = request.pathOrNull("projectId")
            // ...
            next(request)
        }
    }

    suspendedGate.then(
        routes(
            "/projects/{projectId}" GET { /* ... */ },
            "/projects/{projectId}/settings" GET { /* ... */ }
        )
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Request.pathOrNull(name: String): String?
    ```
  </TabItem>
</Tabs>

### Request.pathWithQuery()
The request's path with its query string appended when it has one, the part of the URL to hand back to a
browser as a relative destination.

<Tabs>
<TabItem value="example" label="Example" default>
    ```kotlin
    // GET /search?q=blue
    request.pathWithQuery()  // "/search?q=blue"
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Request.pathWithQuery(): String
    ```
  </TabItem>
</Tabs>

### Request.sort()
Returns a [Sort](/docs/api/invirt-data/sort) object from the query parameter `sort` in this request or `null` if this parameter is not present.
This should be in the form `sort=<field>:<order>`, for example `sort=name:Asc`, `sort=createdAt:DESC`. The order element
is not case-sensitive.

<Tabs>
<TabItem value="example" label="Example" default>
    ```kotlin
    val sort = request.sort()
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Request.sort(): Sort?
    ```
  </TabItem>
</Tabs>


### Request.page()
Returns a [Page](/docs/api/invirt-data/page#page) object from the query parameters `from` and `size`,
or a `Page` with the values from the default arguments when one or both these parameters are missing (see Declaration below).


<Tabs>
<TabItem value="example" label="Example" default>
    ```kotlin
    val page = request.page()
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun Request.page(
        defaultFrom: Int = 0,
        defaultSize: Int = 10,
        maxSize: Int = defaultSize
    ): Page
    ```
  </TabItem>
</Tabs>
