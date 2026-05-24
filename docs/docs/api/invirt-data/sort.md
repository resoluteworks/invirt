---
sidebar_position: 3
---

# Sort

Invirt sorts on a single field at a time. This keeps API and URL surface small; applications that
need compound sort can compose the underlying database constructs directly.

```kotlin
data class Sort(
    val field: String,
    val order: SortOrder
) {
    fun revert(): Sort                 // returns Sort with flipped order
    override fun toString(): String    // "field:asc" or "field:desc"

    companion object {
        operator fun invoke(sortString: String): Sort   // parses "field:asc"
        fun asc(field: String): Sort
        fun desc(field: String): Sort
    }
}

enum class SortOrder { ASC, DESC ;
    fun revert(): SortOrder
    companion object {
        fun fromString(orderString: String): SortOrder  // case-insensitive
    }
}
```

Convenience builders are available on `String` and `KProperty`:

```kotlin
"createdAt".sortAsc()       // Sort(createdAt, ASC)
"createdAt".sortDesc()      // Sort(createdAt, DESC)

Order::createdAt.sortAsc()  // Sort(createdAt, ASC)
Order::createdAt.sortDesc() // Sort(createdAt, DESC)
```

## Sort in query parameters
The `sort` query parameter is in the form `sort=<field>:<order>` (case-insensitive on the order). Use
[`Request.sort()`](/docs/api/invirt-core/request-extensions#requestsort) to read it:

```kotlin
"/list-orders" GET { request ->
    val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
    // ...
}
```

In Pebble templates, [`request.sort`](/docs/framework/current-request#invirtrequest) exposes the same
information and `request.sortIs` checks the current sort order:

```html
{# Currently sorting by createdAt:desc? #}
{% if request.sortIs('createdAt', 'desc') %} ↓ {% endif %}
```

### Manipulating sort
```html
{#
   Returns a Uri with the sort replaced to sort=name:asc and resets pagination.
   /test?q=john&from=100&size=10&sort=createdAt:desc
   becomes
   /test?q=john&sort=name:asc
#}
<a href="{{ request.replaceSort('name', 'asc', true) }}">
    Sort by name ascending
</a>

{#
   Returns a Uri with the sort replaced to createdAt:desc if a different field is being sorted on.
   If createdAt is already the sort field, the order is reverted.
   /test?sort=createdAt:desc → /test?sort=createdAt:asc
   /test?sort=name:asc       → /test?sort=createdAt:desc
#}
<a href="{{ request.revertOrSetSort('createdAt', 'desc', true) }}">
    Created at
</a>
```
