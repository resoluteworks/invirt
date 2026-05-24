---
sidebar_position: 3
---

# Collection operations

Extensions on `MongoCollection<Doc>` that handle versioning, timestamps and ergonomic lookups.

## insert
Inserts a document. When `Doc` is a [`TimestampedDocument`](/docs/api/invirt-mongodb/documents#timestampeddocument),
`createdAt` and `updatedAt` are set to `mongoNow()`. The transactional variants additionally initialise
`version = 1`.

```kotlin
fun <Doc : Any> MongoCollection<Doc>.insert(document: Doc): Doc

fun <Doc : Any> MongoCollection<Doc>.txInsert(session: ClientSession, document: Doc): Doc
fun <Doc : Any> MongoCollection<Doc>.txInsertMany(session: ClientSession, documents: List<Doc>)
```

## update
Updates a document, enforcing optimistic locking on
[`VersionedDocument.version`](/docs/api/invirt-mongodb/documents#versioneddocument). On success the
new `version` is `oldVersion + 1`; on a version conflict, a `VersionConflictException` is thrown
unless a `patchOnConflict` lambda is supplied. When provided, the lambda receives the freshly fetched
document and returns a patched one to retry the update with.

```kotlin
fun <Doc : VersionedDocument> MongoCollection<Doc>.update(
    document: Doc,
    patchOnConflict: ((Doc) -> Doc)? = null
): Doc

fun <Doc : VersionedDocument> MongoCollection<Doc>.txUpdate(
    session: ClientSession,
    document: Doc,
    patchOnConflict: ((Doc) -> Doc)? = null
): Doc
```

`updatedAt` is bumped automatically when `Doc` is a `TimestampedDocument`.

## Lookups
```kotlin
fun <Doc : Any> MongoCollection<Doc>.get(id: String): Doc?
fun <Doc : Any> MongoCollection<Doc>.txGet(session: ClientSession, id: String): Doc?

fun <Doc : Any> MongoCollection<Doc>.findOne(filter: Bson): Doc?       // throws if >1 match
fun <Doc : Any> MongoCollection<Doc>.txFindOne(session: ClientSession, filter: Bson): Doc?
fun <Doc : Any> MongoCollection<Doc>.findFirst(filter: Bson, sort: Bson): Doc?

fun <Doc : Any> MongoCollection<Doc>.findByIds(vararg ids: String): List<Doc>
fun <Doc : Any> MongoCollection<Doc>.findByIds(ids: List<String>): List<Doc>

fun <Doc : Any> MongoCollection<Doc>.findIds(filter: Bson): Set<String> // projection over _id
```

## Deletion
```kotlin
fun MongoCollection<*>.delete(id: String): Boolean
fun MongoCollection<*>.txDelete(session: ClientSession, id: String): Boolean
```

## Pagination
A single call that combines find, sort, page and count into a [`RecordsPage`](/docs/api/invirt-data/page#recordspage):

```kotlin
fun <Doc : Any> MongoCollection<Doc>.pagedQuery(
    filter: Bson = Filters.empty(),
    page: Page = Page(0, 10),
    sort: List<Bson> = emptyList(),
    maxDocuments: Int = 0,
    buildFind: FindIterable<Doc>.() -> FindIterable<Doc> = { this }
): RecordsPage<Doc>

// single-sort convenience overload
fun <Doc : Any> MongoCollection<Doc>.pagedQuery(
    filter: Bson = Filters.empty(),
    page: Page = Page(0, 10),
    sort: Bson,
    maxDocuments: Int = 0,
    buildFind: FindIterable<Doc>.() -> FindIterable<Doc> = { this }
): RecordsPage<Doc>
```

`maxDocuments = 0` means "count all matches". A non-zero value caps the count for very large
collections where an exact count is too expensive.

## MongoQuery (builder)
A fluent builder over the same primitives, useful for composing operations from request parameters:

```kotlin
val page: RecordsPage<Product> = collection.query()
    .andFilter(Product::priceMinor.mongoGte(1000_00), Product::name.mongoEq("Apple"))
    .sort(Product::createdAt.mongoSortDesc())
    .page(request.page())
    .collation(caseInsensitive())
    .find()
```

`MongoQuery.find()` returns a `RecordsPage<Doc>`.
