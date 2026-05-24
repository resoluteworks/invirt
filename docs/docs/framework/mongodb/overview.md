---
sidebar_position: 1
---

# Overview

`invirt-mongodb` is a thin Kotlin layer on top of the [MongoDB Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/).
It provides:

* A small `Mongo` wrapper for managing the client lifecycle and running transactions.
* Document interfaces (`VersionedDocument`, `TimestampedDocument`) with optimistic locking and managed timestamps.
* Strongly-typed filters, sorts and indexes via `KProperty` extensions.
* A bridge from Invirt's [`DataFilter`](/docs/api/invirt-data/data-filter) / [`Sort`](/docs/api/invirt-data/sort) /
  [`Page`](/docs/api/invirt-data/page) abstractions to native MongoDB constructs.
* A cursor-based pagination helper for aggregation pipelines.
* Helpers for [Atlas Search](https://www.mongodb.com/products/platform/atlas-search) and for running
  [Mongock](https://www.mongock.io/) migrations.

## Dependency
```kotlin
implementation(platform("dev.invirt:invirt-bom:${invirtVersion}"))
implementation("dev.invirt:invirt-mongodb")
```

## Connecting

The `Mongo` class wraps a `MongoClient` and is the entry point for the rest of the API. The database
name is extracted from the connection string &mdash; an explicit database name is required.

```kotlin
val mongo = Mongo("mongodb://localhost:27017/myapp")

mongo.database // com.mongodb.kotlin.client.MongoDatabase
mongo.databaseName // "myapp"

mongo.runInTransaction { session ->
    // ...
}

mongo.close()
```

`runInTransaction` starts a session with `WriteConcern.MAJORITY` and either commits or aborts based
on the block outcome.

## Documents

`VersionedDocument` and `TimestampedDocument` are interfaces your domain documents can implement to
opt into optimistic locking and managed `createdAt` / `updatedAt` timestamps.

```kotlin
data class Product(
    override val id: String = uuid7(),
    val name: String,
    val priceMinor: Long,
    override var version: Long = 0,
    override var createdAt: Instant = mongoNow(),
    override var updatedAt: Instant = mongoNow()
) : TimestampedDocument
```

`MongoCollection.insert(document)` and `MongoCollection.update(document)` (and their transactional
`txInsert` / `txUpdate` counterparts) handle `version` increment and timestamp maintenance
automatically. See [Collection operations](/docs/api/invirt-mongodb/collection) for the full surface.

## Indexes

Indexes can be defined fluently from a string or a `KProperty`:

```kotlin
collection.createIndices(
    Product::name.asc(),
    Product::priceMinor.desc(),
    Product::name.asc { caseInsensitive() },
    *TimestampedDocument.allIndices()
)
```

`TimestampedDocument.allIndices()` returns the version + createdAt + updatedAt indices used by the
framework's optimistic locking and timestamp handling.

## Querying

For ad-hoc queries the standard Kotlin driver API works as-is. For composing filters, sort and
pagination together, use `MongoQuery`:

```kotlin
val results: RecordsPage<Product> = collection.query()
    .andFilter(Product::priceMinor.mongoGte(1000_00), Product::name.mongoIn("Apple", "Pear"))
    .sort(Product::createdAt.mongoSortDesc())
    .page(request.page())
    .find()
```

`MongoCollection.pagedQuery(...)` exposes the same operation as a single call when you already have
the filter/sort/page constructed elsewhere. To translate Invirt's request-derived
[`DataFilter`](/docs/api/invirt-data/data-filter) into a MongoDB filter, use `.mongoFilter()`:

```kotlin
"/products" GET { request ->
    val filter = productFilter(request)?.mongoFilter() ?: Filters.empty()
    val results = collection.pagedQuery(filter, request.page(), request.sort()?.mongoSort()?.let { listOf(it) } ?: emptyList())
    // ...
}
```

## What's next

See the API reference for the complete surface:

* [Collection operations](/docs/api/invirt-mongodb/collection)
* [Filters and sorts](/docs/api/invirt-mongodb/filters)
* [Indexes](/docs/api/invirt-mongodb/indexes)
* [Aggregates](/docs/api/invirt-mongodb/aggregates)
* [Cursor pagination](/docs/api/invirt-mongodb/cursor)
* [Atlas Search](/docs/api/invirt-mongodb/atlas)
* [Mongock migrations](/docs/api/invirt-mongodb/mongock)
* [Batch operations](/docs/api/invirt-mongodb/batch)
