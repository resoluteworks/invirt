package invirt.mongo.test

import com.mongodb.kotlin.client.MongoCollection
import invirt.mongodb.Mongo
import io.mockk.every
import io.mockk.spyk

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
