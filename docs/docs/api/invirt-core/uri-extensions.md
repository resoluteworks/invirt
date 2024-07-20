---
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Uri Extensions

### hasQueryValue
Checks whether this Uri has a query parameter with the specified `name` and `value`.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // returns true
    Uri.of("/test?q=kotlin").hasQueryValue("q", "kotlin")

    // returns false
    Uri.of("/test?size=large").hasQueryValue("size", "small")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.hasQueryValue(name: String, value: String): Boolean
    ```
  </TabItem>
</Tabs>


### removeQueryValue
Returns a new `Uri` based on this Uri and without the specified `name` parameter, if the parameter's value matches
the specified `value`.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Returns "/test"
    Uri.of("/test?q=John").removeQueryValue("q", "John")

    // Returns "/test?q=kotlin"
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
Returns a new `Uri` based on this Uri and:
 * Adds a query parameter with the specified `name` and `value` when one isn't already present
 * Removes the query parameter with the specified `name` and `value` when present

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Returns "/test?q=John"
    Uri.of("/test").toggleQueryValue("q", "John")

    // Returns "/test"
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
Removes all query parameters with the specified `names` (immaterial of their values).

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Returns "/test"
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
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Returns "/test?q=John&size=10"
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
Returns a new `Uri` with the page parameters (`&from=x&size=y`) replaced with new values matching
the specified `invirt.data.Page`.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Returns "/test?from=10&size=5"
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
Returns a new `Uri` with the sort query param (`&sort=name:asc`) replaced with a new value matching
the specified `sort` argument. The function resets pagination by removing the `from` and `size` query parameters. This can be disabled
by passing `false` for the `resetPagination` argument.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Returns "/test?sort=createdAt:desc"
    Uri.of("/test?sort=name:asc").replaceSort(Sort.desc("createdAt"))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Uri.replaceSort(sort: Sort, resetPagination: Boolean = true): Uri
    ```
  </TabItem>
</Tabs>

