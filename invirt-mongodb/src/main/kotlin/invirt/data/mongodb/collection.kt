package invirt.data.mongodb

import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.MongoCollection
import org.bson.conversions.Bson

fun <T : Any> MongoCollection<T>.findOne(filter: Bson): T? {
    val list = find(filter).toList()
    if (list.size > 1) {
        throw IllegalStateException("More than one document found for filter $filter")
    }
    return list.firstOrNull()
}

fun <T : Any> MongoCollection<T>.get(id: String): T? {
    return findOne(byId(id))
}

fun <T : Any> MongoCollection<T>.delete(id: String): Boolean {
    return deleteOne(byId(id)).deletedCount == 1L
}

/**
 * Essentially an upsert operation
 */
fun <T : Any> MongoCollection<T>.save(id: String, entity: T): T {
    replaceOne(byId(id), entity, ReplaceOptions().upsert(true))
    return entity
}
