package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Updates
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.ClientSession
import com.mongodb.kotlin.client.MongoCollection
import org.bson.Document
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
 * Transactional version of inserting multiple documents.
 */
fun <Doc : Any> MongoCollection<Doc>.txInsertMany(session: ClientSession, documents: List<Doc>) {
    documents.forEach { document ->
        if (document is TimestampedDocument) {
            document.createdAt = mongoNow()
            document.updatedAt = mongoNow()
        }
        if (document is VersionedDocument) {
            document.version = 1
        }
    }
    insertMany(session, documents)
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

/**
 * Applies [updates] to the single document matching [filter], incrementing [VersionedDocument.version]
 * in the same atomic operation so the write is not invisible to the optimistic lock: without the bump,
 * a concurrent [update] holding a copy loaded beforehand still matches on version and silently replaces
 * whatever was written here.
 *
 * This is the partial-write counterpart of [update] and a different discipline from it. The write is a
 * `$set`-style update of the fields in [updates] rather than a whole-document replace, it never throws
 * [VersionConflictException] (a [filter] that matches nothing is a zero-count [UpdateResult], not a
 * failure), and the caller's in-memory document is left as it was - re-read it to see the new state.
 * That makes it the tool for a conditional write whose filter *is* the concurrency control, and [update]
 * the tool for saving an edited document.
 *
 * [TimestampedDocument.updatedAt] is deliberately left alone: use [timestampedUpdateOne] when the write
 * should move it. They are separate because a write that must not disturb an `updatedAt desc` listing
 * has no other way to say so.
 */
fun <Doc : VersionedDocument> MongoCollection<Doc>.versionedUpdateOne(filter: Bson, vararg updates: Bson): UpdateResult =
    updateOne(filter, versionedUpdate(updates))

/**
 * Transactional version of [versionedUpdateOne].
 */
fun <Doc : VersionedDocument> MongoCollection<Doc>.txVersionedUpdateOne(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson
): UpdateResult = updateOne(session, filter, versionedUpdate(updates))

/**
 * [versionedUpdateOne] returning the matched document, or `null` when [filter] matched nothing.
 *
 * [options] carries the usual `findOneAndUpdate` choices; by default the document is returned as it was
 * *before* the update, so pass `FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)` for the
 * updated one.
 *
 * The `null` is what makes this a race winner check: concurrent callers of the same conditional update
 * all run, and exactly the one whose filter still matched gets a document back.
 */
fun <Doc : VersionedDocument> MongoCollection<Doc>.versionedFindOneAndUpdate(
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc? = findOneAndUpdate(filter, versionedUpdate(updates), options)

/**
 * Transactional version of [versionedFindOneAndUpdate].
 */
fun <Doc : VersionedDocument> MongoCollection<Doc>.txVersionedFindOneAndUpdate(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc? = findOneAndUpdate(session, filter, versionedUpdate(updates), options)

/**
 * [versionedUpdateOne] that also sets [TimestampedDocument.updatedAt] to [mongoNow], which is what an
 * ordinary partial write to a timestamped document should do. [TimestampedDocument.createdAt] is left
 * alone.
 */
fun <Doc : TimestampedDocument> MongoCollection<Doc>.timestampedUpdateOne(filter: Bson, vararg updates: Bson): UpdateResult =
    updateOne(filter, timestampedUpdate(updates))

/**
 * Transactional version of [timestampedUpdateOne].
 */
fun <Doc : TimestampedDocument> MongoCollection<Doc>.txTimestampedUpdateOne(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson
): UpdateResult = updateOne(session, filter, timestampedUpdate(updates))

/**
 * [timestampedUpdateOne] returning the matched document, or `null` when [filter] matched nothing.
 * See [versionedFindOneAndUpdate] for [options] and for what the `null` means.
 */
fun <Doc : TimestampedDocument> MongoCollection<Doc>.timestampedFindOneAndUpdate(
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc? = findOneAndUpdate(filter, timestampedUpdate(updates), options)

/**
 * Transactional version of [timestampedFindOneAndUpdate].
 */
fun <Doc : TimestampedDocument> MongoCollection<Doc>.txTimestampedFindOneAndUpdate(
    session: ClientSession,
    filter: Bson,
    vararg updates: Bson,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): Doc? = findOneAndUpdate(session, filter, timestampedUpdate(updates), options)

private fun versionIncrement(): Bson = Updates.inc(VersionedDocument::version.name, 1L)

private fun timestampedStamps(): List<Bson> = listOf(
    Updates.set(TimestampedDocument::updatedAt.name, mongoNow()),
    versionIncrement()
)

private fun versionedUpdate(updates: Array<out Bson>): Bson = Updates.combine(updates.toList().plus(versionIncrement()))

private fun timestampedUpdate(updates: Array<out Bson>): Bson = Updates.combine(updates.toList().plus(timestampedStamps()))

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

/**
 * Finds documents matching the specified [filter] and returns a set of their `_id`'s.
 */
fun <Doc : Any> MongoCollection<Doc>.findIds(filter: Bson): Set<String> = withDocumentClass<Document>()
    .find(filter)
    .projection(include("_id"))
    .toList()
    .map { it.getString("_id") }
    .toSet()
