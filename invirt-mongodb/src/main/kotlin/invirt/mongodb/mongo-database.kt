package invirt.mongodb

import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase

inline fun <reified E : StoredEntity> MongoDatabase.collection(): MongoCollection<E> {
    val collection = getCollection<E>(collectionName<E>())
    collection.createEntityIndexes()
    return collection
}
