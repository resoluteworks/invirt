---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Forms

### Request.toForm()
Converts a `application/x-www-form-urlencoded` request body to an instance of the given type, with
support for arrays, maps and nested objects. Field names use dot notation for nesting and square brackets
for arrays/maps:

```
parent.children[0].name=John
departments[HR].employees[0].age=32
```

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    "/save-order" POST { request ->
        val form = request.toForm<OrderForm>()
        // ...
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    inline fun <reified T : Any> Request.toForm(): T
    fun <T : Any> Request.toForm(formClass: KClass<T>): T
    ```
  </TabItem>
</Tabs>

See the [Form basics](/docs/framework/forms/form-basics) section for a worked example.

### Request.queryToForm()
Same as `toForm()` but reads from query string parameters instead of a form-encoded body. Useful for
binding rich query strings (filters, search criteria) to a typed Kotlin object.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    "/search" GET { request ->
        val criteria = request.queryToForm<SearchCriteria>()
        // ...
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    inline fun <reified T : Any> Request.queryToForm(): T
    fun <T : Any> Request.queryToForm(formClass: KClass<T>): T
    ```
  </TabItem>
</Tabs>
