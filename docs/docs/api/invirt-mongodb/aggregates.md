---
sidebar_position: 6
---

# Aggregates

## pagedAggregate
Runs an aggregation pipeline and returns a [`RecordsPage`](/docs/api/invirt-data/page#recordspage) plus
the raw aggregation result. Internally wraps the pipeline in a `$facet` stage with `documents` (skip +
limit) and `totalCount` facets, plus any additional facets you pass in.

```kotlin
fun <Doc : Any> MongoCollection<Doc>.pagedAggregate(
    pipeline: List<Bson>,
    page: Page,
    facets: List<Facet> = emptyList()
): PagedAggregateSearchResult<Doc>

class PagedAggregateSearchResult<Doc : Any>(
    val recordsPage: RecordsPage<Doc>,
    val rawResult: Document
)
```

The `rawResult` holds the full `$facet` response, so you can read any extra facets you passed in:

```kotlin
val result = collection.pagedAggregate(
    pipeline = listOf(Aggregates.match(filter)),
    page = Page(0, 20),
    facets = listOf(
        Facet("byCategory", Aggregates.group("\$category", Accumulators.sum("count", 1)))
    )
)

result.recordsPage.records       // List<Doc>
result.recordsPage.totalCount
result.rawResult["byCategory"]   // raw aggregation output
```
