package invirt.mongodb

import java.time.Instant

/**
 * Represents a records stored in Mongo that can be used for optimistic lock
 * scenarios using the [version] property.
 */
interface VersionedDocument {
    val id: String
    var version: Long
}

/**
 * Defines created and updated timestamps for a MongoDB document. These are automatically
 * handled by the framework when using [com.mongodb.kotlin.client.MongoCollection.insert]
 * or [com.mongodb.kotlin.client.MongoCollection.update], or their respective transactional versions.
 */
interface TimestampedDocument : VersionedDocument {
    var createdAt: Instant
    var updatedAt: Instant
}
