---
sidebar_position: 3
---

# Pagination

Invirt takes a somewhat opinionated approach for the design of pagination components, but
an approach that's in line with certain user experience constraints in many applications:
 * The user needs to know the total number of pages for the result set (paging to unknown is not an option)
 * There is a concept of a first page, current page, and last page
 * The user needs to know the total number of results in the current query.


# Page

To that effect, the core pagination component is kept very simple.
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
This can be used to serve complete information about a results page, including the total count
for the query, the [Page](#page) reference that it was queried with.
```kotlin
data class RecordsPage<T : Any>(
    val records: List<T>,
    val totalCount: Long,
    val page: Page
)
```

## Pagination
Probably the _most_ opinionated component in this section is `Pagination` which
