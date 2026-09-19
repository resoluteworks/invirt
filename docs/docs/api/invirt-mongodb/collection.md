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

## Partial updates
The partial-write counterparts of `update`, for a write that must be atomic: they apply the `updates`
they are given to the single document matching `filter` and increment `version` in the same operation,
so the write is not invisible to the optimistic lock. Without that bump, a concurrent `update` holding
a copy loaded beforehand still matches on version and silently replaces what was written here.

```kotlin
fun <Doc : VersionedDocument> MongoCollection<Doc>.versionedUpdateOne(
    filter: Bson,
    vararg updates: Bson
): UpdateResult

fun <Doc : VersionedDocument> MongoCollection<Doc>.txVersionedUpdateOne(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson
): UpdateResult

fun <Doc : VersionedDocument> MongoCollection<Doc>.versionedFindOneAndUpdate(
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc?

fun <Doc : VersionedDocument> MongoCollection<Doc>.txVersionedFindOneAndUpdate(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc?

fun <Doc : TimestampedDocument> MongoCollection<Doc>.timestampedUpdateOne(
    filter: Bson,
    vararg updates: Bson
): UpdateResult

fun <Doc : TimestampedDocument> MongoCollection<Doc>.txTimestampedUpdateOne(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson
): UpdateResult

fun <Doc : TimestampedDocument> MongoCollection<Doc>.timestampedFindOneAndUpdate(
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc?

fun <Doc : TimestampedDocument> MongoCollection<Doc>.txTimestampedFindOneAndUpdate(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc?
```

Each has a `tx` variant taking a `ClientSession` as the first parameter, for use inside
[`Mongo.runInTransaction`](/docs/api/invirt-mongodb/mongo).

This is a different discipline from `update`. The write is a `$set`-style update of the named fields
rather than a whole-document replace, it never throws `VersionConflictException` (a filter that matches
nothing is a zero-count `UpdateResult` or a `null`, not a failure), and the caller's in-memory document
is left as it was - re-read it to see the new state. That makes it the tool for a conditional write
whose filter *is* the concurrency control, and `update` the tool for saving an edited document:

```kotlin
// Exactly one of the racing callers gets a document back
val won = openCalls.versionedFindOneAndUpdate(
    mongoAnd(mongoById(id), Filters.ne(OpenCall::milestonesNotified.name, threshold)),
    Updates.addEachToSet(OpenCall::milestonesNotified.name, crossed)
) != null
```

The `timestamped*` pair additionally sets `updatedAt` to `mongoNow()`, which is what an ordinary partial
write to a timestamped document should do. The `versioned*` pair deliberately leaves it alone, for a
write that must not disturb an `updatedAt desc` listing. `createdAt` is never touched.

`versionedFindOneAndUpdate` and `timestampedFindOneAndUpdate` return the document as it was *before*
the update unless `options` says otherwise:

```kotlin
options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
```

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
