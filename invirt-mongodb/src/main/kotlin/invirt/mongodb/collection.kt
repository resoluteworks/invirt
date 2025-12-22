package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.ClientSession
import com.mongodb.kotlin.client.MongoCollection
import org.bson.conversions.Bson

/**
 * Creates a new record for the specified [document]. It handles the initialisation of [VersionedDocument.version]
 * as well as [TimestampedDocument.createdAt] / [TimestampedDocument.updatedAt] when applicable.
 */
fun <Doc : Any> MongoCollection<Doc>.insert(document: Doc): Doc {
    if (document is TimestampedDocument) {
        document.createdAt = mongoNow()
        document.updatedAt = mongoNow()
    }
    insertOne(document)
    return document
}

/**
 * Transactional version of [MongoCollection.insert].
 */
fun <Doc : Any> MongoCollection<Doc>.txInsert(session: ClientSession, document: Doc): Doc {
    if (document is TimestampedDocument) {
        document.createdAt = mongoNow()
        document.updatedAt = mongoNow()
    }
    if (document is VersionedDocument) {
        document.version = 1
    }
    insertOne(session, document)
    return document
}

/**
 * Updates the specified [document] with an optimistic lock check based on [VersionedDocument.version].
 * The version of the document is incremented by 1 before the document is updated.
 *
 * When the document is an instance of [TimestampedDocument], [TimestampedDocument.updatedAt] is set
 * to the current time.
 *
 * An optional [patchOnConflict] can be provided to re-apply the client updates on a fresh copy of the
 * document when the optimistic lock fails (version drifted). When [patchOnConflict] is `null` and the
 * optimistic lock check fails, a [VersionConflictException] is thrown. If the update with the patched
 * document fails, a [VersionConflictException] is thrown as well.
 */
fun <Doc : VersionedDocument> MongoCollection<Doc>.update(
    document: Doc,
    patchOnConflict: ((Doc) -> Doc)? = null
): Doc = update(null, document, patchOnConflict)

/**
 * Transactional version of [MongoCollection.update]
 */
fun <Doc : VersionedDocument> MongoCollection<Doc>.txUpdate(
    session: ClientSession,
    document: Doc,
    patchOnConflict: ((Doc) -> Doc)? = null
): Doc = update(session, document, patchOnConflict)

private fun <Doc : VersionedDocument> MongoCollection<Doc>.update(
    session: ClientSession?,
    document: Doc,
    patchOnConflict: ((Doc) -> Doc)?
): Doc = updateOne(session, document)
    ?: if (patchOnConflict != null) {
        // Try the update with the patched version of the document
        val patchedDocument = updateOne(session, patchOnConflict(get(document.id)!!))
        patchedDocument ?: throw VersionConflictException(document.id, document.version)
    } else {
        throw VersionConflictException(document.id, document.version)
    }

private fun <Doc : VersionedDocument> MongoCollection<Doc>.updateOne(clientSession: ClientSession?, document: Doc): Doc? {
    val filter = Filters.and(
        mongoById(document.id),
        VersionedDocument::version.mongoEq(document.version)
    )

    document.version += 1
    if (document is TimestampedDocument) {
        document.updatedAt = mongoNow()
    }

    val isUpdated = clientSession
        ?.let { replaceOne(clientSession, filter, document).matchedCount == 1L }
        ?: (replaceOne(filter, document).matchedCount == 1L)

    return if (isUpdated) {
        document
    } else {
        null
    }
}

/**
 * Retrieves a document by its [id] or `null` if no document is found.
 */
fun <Doc : Any> MongoCollection<Doc>.get(id: String): Doc? = findOne(mongoById(id))
fun <Doc : Any> MongoCollection<Doc>.txGet(session: ClientSession, id: String): Doc? = txFindOne(session, mongoById(id))

/**
 * Find the first document matching the specified [filter] or `null` if no document is found.
 */
fun <Doc : Any> MongoCollection<Doc>.findOne(filter: Bson): Doc? {
    val list = find(filter).toList()
    if (list.size > 1) {
        throw IllegalStateException("Multiple MongoDB documents found for filter $filter")
    }
    return list.firstOrNull()
}

/**
 * Find the first document matching the specified [filter] or `null` if no document is found.
 */
fun <Doc : Any> MongoCollection<Doc>.txFindOne(session: ClientSession, filter: Bson): Doc? {
    val list = find(session, filter).toList()
    if (list.size > 1) {
        throw IllegalStateException("Multiple MongoDB documents found for filter $filter")
    }
    return list.firstOrNull()
}

/**
 * Find the first document matching the specified [filter] and [sort] or `null` if no document is found.
 */
fun <Doc : Any> MongoCollection<Doc>.findFirst(filter: Bson, sort: Bson): Doc? = find(filter)
    .sort(sort)
    .limit(1)
    .toList()
    .firstOrNull()

/**
 * Deletes the document with the specified [id] and returns `true` if the document was deleted.
 */
fun MongoCollection<*>.delete(id: String): Boolean = deleteOne(mongoById(id)).deletedCount == 1L

/**
 * Transactional version of [MongoCollection.delete]
 */
fun MongoCollection<*>.txDelete(session: ClientSession, id: String): Boolean =
    deleteOne(session, mongoById(id)).deletedCount == 1L

/**
 * Finds documents by their [ids] and returns a list of documents.
 */
fun <Doc : Any> MongoCollection<Doc>.findByIds(vararg ids: String): List<Doc> = findByIds(ids.toList())

/**
 * Finds documents by their [ids] and returns a list of documents.
 */
fun <Doc : Any> MongoCollection<Doc>.findByIds(ids: List<String>): List<Doc> = if (ids.isNotEmpty()) {
    find(mongoByIds(ids)).toList()
} else {
    emptyList()
}
