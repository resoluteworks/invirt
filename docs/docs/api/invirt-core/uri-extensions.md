---
sidebar_position: 4
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Uri Extensions

Kotlin extensions on `org.http4k.core.Uri` for the URI manipulation patterns common in
server-rendered pages: filter chips, pagination links, sort headers, etc. The same operations are
exposed on [`InvirtRequest`](/docs/framework/current-request#invirtrequest) for use inside Pebble
templates.

### hasQueryParam
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun Uri.hasQueryParam(name: String): Boolean
    ```
  </TabItem>
</Tabs>

### hasQueryValue
Checks whether the URI has a query parameter with the given `name` and `value`.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // true
    Uri.of("/test?q=kotlin").hasQueryValue("q", "kotlin")

    // false
    Uri.of("/test?size=large").hasQueryValue("size", "small")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.hasQueryValue(name: String, value: String): Boolean
    ```
  </TabItem>
</Tabs>

### queryValue
Returns the first value of the named query parameter, case-insensitive on the parameter name.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun Uri.queryValue(name: String): String?
    ```
  </TabItem>
</Tabs>

### removeQueryValue
Returns a new `Uri` without the specified `name`/`value` pair. All other query params are left unchanged.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // "/test"
    Uri.of("/test?q=John").removeQueryValue("q", "John")

    // "/test?q=kotlin"
    Uri.of("/test?q=kotlin&q=java").removeQueryValue("q", "java")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.removeQueryValue(name: String, value: Any): Uri
    ```
  </TabItem>
</Tabs>

### toggleQueryValue
Returns a new `Uri` with the specified `name`/`value` pair added when not present, or removed when
present. All other query params are left unchanged.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // "/test?q=John"
    Uri.of("/test").toggleQueryValue("q", "John")

    // "/test"
    Uri.of("/test?q=John").toggleQueryValue("q", "John")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.toggleQueryValue(name: String, value: Any): Uri
    ```
  </TabItem>
</Tabs>

### removeQueries
Removes all query parameters with the given names (regardless of value).

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // "/test"
    Uri.of("/test?q=john&filter=name").removeQueries(listOf("q", "filter"))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.removeQueries(names: Collection<String>): Uri
    ```
  </TabItem>
</Tabs>

### replaceQuery
Replaces the given query parameters with new values. All other query params are left unchanged.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // "/test?q=John&size=10"
    Uri.of("/test?q=nothing&size=5").replaceQuery("q" to "John", "size" to "10")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.replaceQuery(vararg queryValues: Pair<String, Any>): Uri
    fun Uri.replaceQuery(queries: Map<String, Any>): Uri
    ```
  </TabItem>
</Tabs>

### replacePage
Returns a new `Uri` with the `from`/`size` pagination parameters replaced to match the given `Page`.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // "/test?from=10&size=5"
    Uri.of("/test?from=0&size=10").replacePage(Page(10, 5))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.replacePage(page: Page): Uri
    ```
  </TabItem>
</Tabs>

### replaceSort
Returns a new `Uri` with the `sort` query parameter replaced to match the given `Sort`. By default
pagination (`from`/`size`) is reset; pass `resetPagination = false` to keep it.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // "/test?sort=createdAt:desc"
    Uri.of("/test?sort=name:asc").replaceSort(Sort.desc("createdAt"))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.replaceSort(sort: Sort, resetPagination: Boolean = true): Uri
    ```
  </TabItem>
</Tabs>

### CSV-encoded multi-value parameters
For situations where multiple values are encoded into a single comma-separated query parameter
(`?tags=red,green,blue`), Invirt provides helpers to read and mutate the list:

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // ["red", "green"]
    Uri.of("/?tags=red,green").csvQuery("tags")

    // "/?tags=red,green,blue"
    Uri.of("/?tags=red,green").csvAppend("tags", "blue")

    // "/?tags=red"
    Uri.of("/?tags=red,green").csvRemove("tags", "green")

    // Toggles: add if missing, remove if present
    Uri.of("/?tags=red,green").csvToggle("tags", "blue")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.csvQuery(name: String): List<String>
    fun Uri.csvAppend(name: String, value: Any): Uri
    fun Uri.csvRemove(name: String, value: Any): Uri
    fun Uri.csvToggle(name: String, value: Any): Uri
    ```
  </TabItem>
</Tabs>
