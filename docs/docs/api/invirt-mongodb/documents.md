---
sidebar_position: 2
---

# Documents

Two opt-in interfaces describe the metadata Invirt manages for you. Implementing either changes the
behaviour of the [`insert`](/docs/api/invirt-mongodb/collection#insert) and
[`update`](/docs/api/invirt-mongodb/collection#update) operations.

## VersionedDocument
Adds an optimistic-locking `version` field. The framework increments `version` on every successful
`update` and rejects updates where the stored version no longer matches the in-memory version.

```kotlin
interface VersionedDocument {
    val id: String
    var version: Long

    companion object {
        fun versionIndex(): IndexModel
    }
}
```

## TimestampedDocument
Extends `VersionedDocument` with `createdAt` and `updatedAt`. The framework sets `createdAt` and
`updatedAt` on `insert`, and `updatedAt` on `update`.

```kotlin
interface TimestampedDocument : VersionedDocument {
    var createdAt: Instant
    var updatedAt: Instant

    companion object {
        fun timestampIndicesList(): List<IndexModel>
        fun timestampIndices(): Array<IndexModel>
        fun allIndices(): Array<IndexModel>     // version + createdAt + updatedAt
    }
}
```

### Example
```kotlin
data class Product(
    override val id: String = uuid7(),
    val name: String,
    val priceMinor: Long,
    override var version: Long = 0,
    override var createdAt: Instant = mongoNow(),
    override var updatedAt: Instant = mongoNow()
) : TimestampedDocument

collection.createIndices(*TimestampedDocument.allIndices())
```

`mongoNow()` returns the current time truncated to millisecond precision &mdash; matching the storage
precision used by MongoDB.
