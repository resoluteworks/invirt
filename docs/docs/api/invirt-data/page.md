---
sidebar_position: 2
---

# Pagination components

Invirt takes a somewhat opinionated approach for the design of pagination components, but
one that's in line with certain user experience constraints in many applications:
 * The user needs to know the total number of pages for the result set (paging to unknown is not an option).
 * There is a concept of a first page, current page, and last page.
 * The user needs to know the total number of results in the current query.


## Page

To that effect, the core pagination component is kept very simple. To read a `Page` object from
a `Request` object's query parameters use the [Request.page()](/docs/api/invirt-core/request-extensions#requestpage)
extension.

```kotlin
data class Page(
    val from: Int,
    val size: Int
) {

    /**
     * Returns the index of the current page relative to the beginning of the
     * pagination (from = 0).
     */
    val pageIndex: Int
}
```

## RecordsPage
The `RecordsPage` component is an abstract representation of a page of records for a result set.
This can be used to serve complete information about the results, including the total count
for the query and the [Page](#page) reference that it was queried with.
```kotlin
data class RecordsPage<T : Any>(
    val records: List<T>,
    val totalCount: Long,
    val page: Page
) {
    val pagination = Pagination(page, totalCount)
}
```

## Pagination
The `Pagination` component defines the logic that can be used to render a pagination control for
a result set. Please check the [component's documentation](https://github.com/resoluteworks/invirt/blob/main/invirt-data/src/main/kotlin/invirt/data/Pagination.kt)
for full details on how this works.
