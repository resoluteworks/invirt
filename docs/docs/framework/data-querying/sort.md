---
sidebar_position: 3
---

# Sort

As most applications require a single sort criteria to be applied at a time, we've kept this
really simple, and we leave it to the application to compound sort logic should it require to.
Although, unless you're designing a data analytics platform, it's likely you won't have to.

```kotlin
data class Sort(
    val field: String,
    val order: SortOrder
)

enum class SortOrder {
    ASC,
    DESC
}
```

## Sort in query parameters
Invirt provides a set of utilities for reading and manipulating sort criteria passed as query
parameters. These are all based on a `sort` query parameter which is hardwired in all these utilities
and is not configurable (at this stage).

### Reading sort
```kotlin
"/list-orders" GET { request ->
    // Create a Sort object from a query parameter sort=name:asc
    val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
    ...
}
```

```html
<!-- Displays in the template the field we're currently sorting by, based on the sort query param -->
{{ request.sort.field }}

<!-- Returns true if the current sort query parameter is sort=name:asc -->
{{ request.sortIs('name', 'asc') }}
```

### Manipulating sort
```html
<!--
    Returns a Uri with the sort field replaced to sort=name:asc and resets
    pagination (last argument, called resetPagination is set to true)

    For example
        /test?q=john&from=100&size=10&sort=createdAt:desc
    becomes
        /test?q=john&sort=name:asc
-->
<a href="{{ request.replaceSort('name', 'asc', true) }}">
    Sort by name ascending
</a>


<!--
    Returns a Uri with the sort field replaced to createdAt:desc IF a
    sort for another field is present. If the current sort is for the
    createdAt field, then it simply reverts its order.

    /test?sort=createdAt:desc → /test?sort=createdAt:asc
    /test?sort=name:asc       → /test?sort=createdAt:desc
-->
<a href="{{ request.revertOrSetSort('createdAt', 'desc', true) }}">
    Created at
</a>
```

