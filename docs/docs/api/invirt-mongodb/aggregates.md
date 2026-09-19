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
    facets: List<Facet> = emptyList(),
    documentStages: List<Bson> = emptyList()
): PagedAggregateSearchResult<Doc>

class PagedAggregateSearchResult<Doc : Any>(
    val recordsPage: RecordsPage<Doc>,
    val rawResult: Document
)
```

`facets` and `documentStages` are both lists of stages and they land in different places. A `Facet` in
`facets` is a sibling of the `documents` facet and runs over the whole matched population.
`documentStages` run *inside* the `documents` facet, after its `$skip` and `$limit`, so they see the
page's rows only - the place for a per-row `$lookup` that would otherwise be paid for across the whole
population:

```kotlin
val result = collection.pagedAggregate(
    pipeline = listOf(Aggregates.match(filter), Aggregates.sort(sort)),
    page = Page(0, 20),
    documentStages = listOf(
        Aggregates.lookup("authors", Book::authorId.name, "_id", "authorLookup"),
        Aggregates.addFields(Field("authorName", mongoLookupField("authorLookup", "name"))),
        Aggregates.unset("authorLookup")
    )
)
```

Because they run after `$skip` / `$limit`, a `$match` or a `$sort` in `documentStages` disagrees with
`totalCount` and with the paging: the match drops rows the count still includes, and the sort only
orders the rows of the page it is handed. Narrowing and ordering belong in `pipeline`.

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

## Reading a $facet result
Two readers over the raw `$facet` response. A `$count` stage emits no document at all when it matched
nothing, so an empty branch - and a branch that is not in the result - read as `0` and as an empty list
respectively.

```kotlin
fun Document.countFacet(name: String): Int
inline fun <reified Doc : Any> Document.facetListOf(name: String): List<Doc>
```

```kotlin
result.rawResult.countFacet("totalCount")             // Int
result.rawResult.facetListOf<Product>("bestSellers")  // List<Product>
```

## aggregateCount
Runs a pipeline and returns the number of documents it leaves, appending the `$count` stage itself so
the caller never names the field the count lands under. A pipeline that matches nothing reads as `0`.

```kotlin
fun MongoCollection<*>.aggregateCount(pipeline: List<Bson>): Long
```

## countsGroupedBy
Counts the documents whose `field` is one of `ids`, grouped by `field`, as a single aggregation rather
than a count per id. `extraFilter` narrows what is counted.

```kotlin
fun <Doc : Any> MongoCollection<Doc>.countsGroupedBy(
    field: KProperty<*>,
    ids: Collection<String>,
    extraFilter: Bson? = null
): Map<String, Int>
```

```kotlin
val counts = orders.countsGroupedBy(
    Order::customerId,
    customerIds,
    Order::status.mongoEq(Order.Status.PLACED)
)
counts[customerId] ?: 0
```

An id with no documents is absent from the map rather than present with a zero. An empty `ids` returns
an empty map without querying, which is correctness rather than an optimisation:
[`mongoIn`](/docs/api/invirt-mongodb/filters) rejects an empty collection. The map is typed to the pair
this counts: a `String` group key, so `field` must hold strings, and an `Int` count.

## Lookup expressions
`mongoLookupField` builds the expression that reads a scalar out of the array a `$lookup` leaves behind,
for a lookup whose sub-pipeline resolves to at most one document. The `$ifNull` wrapper is added only
when a `default` is given; without one the expression resolves to missing on an empty lookup.

```kotlin
fun mongoLookupField(lookupField: String, path: String? = null, default: Any? = null): Document
```

```kotlin
// { $ifNull: [ { $arrayElemAt: [ "$organisationLookup.name", 0 ] }, "Unknown organisation" ] }
Field("organisationName", mongoLookupField("organisationLookup", "name", "Unknown organisation"))

// { $arrayElemAt: [ "$allocationLookup", 0 ] }
Field("allocation", mongoLookupField("allocationLookup"))
```

`mongoCorrelatedEq` builds the `{ $eq: [ "$field", "$$letVariable" ] }` a correlated `$lookup`
sub-pipeline matches on. It is an expression rather than a filter, so a `$match` wraps it in
`Filters.expr`:

```kotlin
fun mongoCorrelatedEq(field: String, letVariable: String): Document
```

```kotlin
Aggregates.lookup(
    "consent-events",
    listOf(Variable("consentUserId", "\$_id")),
    listOf(
        Aggregates.match(Filters.expr(mongoCorrelatedEq(ConsentEvent::userId.name, "consentUserId"))),
        Aggregates.sort(Sorts.descending(ConsentEvent::createdAt.name)),
        Aggregates.limit(1)
    ),
    "latestEvent"
)
```
