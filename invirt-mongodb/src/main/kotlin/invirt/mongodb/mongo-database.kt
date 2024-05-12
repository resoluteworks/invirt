package invirt.mongodb

import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase

inline fun <reified E : StoredEntity> MongoDatabase.collection(): MongoCollection<E> {
    return getCollection<E>(collectionName<E>())
}
