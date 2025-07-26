package invirt.mongodb

import com.mongodb.client.model.IndexModel
import java.time.Instant

/**
 * Represents a records stored in Mongo that can be used for optimistic lock
 * scenarios using the [version] property.
 *
 * See [com.mongodb.kotlin.client.MongoCollection.update] for more information
 * on using optimistic lock with MongoDB.
 */
interface VersionedDocument {
    val id: String
    var version: Long

    companion object {
        fun versionIndex(): IndexModel = VersionedDocument::version.asc()
    }
}

/**
 * Defines created and updated timestamps for a MongoDB document. These are automatically
 * handled by the framework when using [com.mongodb.kotlin.client.MongoCollection.insert]
 * or [com.mongodb.kotlin.client.MongoCollection.update], or their respective transactional versions.
 */
interface TimestampedDocument : VersionedDocument {
    var createdAt: Instant
    var updatedAt: Instant

    companion object {
        fun timestampIndicesList(): List<IndexModel> = listOf(
            TimestampedDocument::createdAt.desc(),
            TimestampedDocument::updatedAt.desc()
        )

        fun timestampIndices(): Array<IndexModel> = arrayOf(
            TimestampedDocument::createdAt.desc(),
            TimestampedDocument::updatedAt.desc()
        )

        fun allIndices(): Array<IndexModel> = arrayOf(
            VersionedDocument.versionIndex(),
            *timestampIndices()
        )
    }
}
