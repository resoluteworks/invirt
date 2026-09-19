package invirt.mongo.test

import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.MongoCollection
import invirt.mongodb.Mongo
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.mongoById
import io.mockk.every
import io.mockk.spyk
import java.time.Instant

/**
 * Creates a spy of the Mongo instance with a spied collection.
 * This allows you to mock the behavior of the collection
 * without affecting the original Mongo instance.
 *
 * @param Document The type of the document in the collection.
 * @param collectionName The name of the collection to spy on.
 * @param buildCollectionSpy A lambda to configure the spied collection.
 * @return A new Mongo instance with the spied collection.
 */
inline fun <reified Document : Any> Mongo.spyCollection(
    collectionName: String,
    buildCollectionSpy: (collection: MongoCollection<Document>) -> Unit
): Mongo {
    val spyMongo = spyk(this)
    val collection = spyk(this.database.getCollection<Document>(collectionName))
    val spyDb = spyk(this.database)
    every { spyDb.getCollection<Document>(collectionName) } returns collection
    every { spyDb.getCollection(collectionName, Document::class.java) } returns collection
    buildCollectionSpy(collection)
    every { spyMongo.database } returns spyDb
    return spyMongo
}

/**
 * Backdates [id]'s `createdAt` to [createdAt], for a spec that needs a document to look older than
 * insertion actually made it - e.g. to exercise a "created in the last N days" filter, which nothing else
 * can pin since [invirt.mongodb.insert] always stamps `createdAt` from [invirt.mongodb.mongoNow].
 *
 * Writes raw: unlike [invirt.mongodb.update], it does not go through the optimistic-lock path, so it
 * neither bumps `version` nor touches `updatedAt`. Only use it to backdate a fixture's creation time, never
 * as a stand-in for a real update.
 */
fun <Doc : TimestampedDocument> MongoCollection<Doc>.setCreatedAt(id: String, createdAt: Instant) {
    updateOne(mongoById(id), Updates.set(TimestampedDocument::createdAt.name, createdAt))
}
