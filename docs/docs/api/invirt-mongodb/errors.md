---
sidebar_position: 11
---

# Errors and miscellaneous

## VersionConflictException
Thrown by [`update`](/docs/api/invirt-mongodb/collection#update) (and `txUpdate`) when the document's
in-memory version no longer matches the stored version, and no `patchOnConflict` lambda was supplied
(or the patched update also conflicted).

```kotlin
class VersionConflictException(
    val documentId: String,
    val updateVersion: Long
) : Exception
```

## MongoException.isDuplicateError
Convenience check for the MongoDB duplicate-key error code (11000), useful for upsert / unique-index
flows that need to distinguish duplicate conflicts from other write errors.

```kotlin
try {
    collection.insert(product)
} catch (e: MongoException) {
    if (e.isDuplicateError()) {
        // already exists
    } else {
        throw e
    }
}
```

## mongoNow
Returns `Instant.now().truncatedTo(ChronoUnit.MILLIS)`, matching the precision MongoDB uses to store
timestamps. Used by the framework when setting `createdAt` / `updatedAt` on
[`TimestampedDocument`](/docs/api/invirt-mongodb/documents#timestampeddocument).

## Document deserialization
For raw aggregation results, two helpers decode `Document` instances into typed objects using the
default Mongo codec registry:

```kotlin
val products: List<Product> = listOfDocuments.mongoDeserializeWith()
val product: Product = document.mongoDeserializeWith()
```
