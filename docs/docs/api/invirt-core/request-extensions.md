---
sidebar_position: 5
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Request extensions

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
