package invirt.mongodb

import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.ClientSession
import com.mongodb.kotlin.client.MongoCollection
import org.bson.conversions.Bson

fun <E : StoredEntity> MongoCollection<E>.save(entity: E): E {
    replaceOne(mongoById(entity.id), entity.updated(), ReplaceOptions().upsert(true))
    return entity
}

fun <E : StoredEntity> MongoCollection<E>.txSave(session: ClientSession, entity: E): E {
    replaceOne(session, mongoById(entity.id), entity.updated(), ReplaceOptions().upsert(true))
    return entity
}

fun <E : StoredEntity> MongoCollection<E>.get(id: String): E? {
    return findOne(mongoById(id))
}

fun <E : StoredEntity> MongoCollection<E>.findOne(filter: Bson): E? {
    val list = find(filter).toList()
    if (list.size > 1) {
        throw IllegalStateException("More than one document found for filter $filter")
    }
    return list.firstOrNull()
}

fun <E : StoredEntity> MongoCollection<E>.delete(id: String): Boolean {
    return deleteOne(mongoById(id)).deletedCount == 1L
}

fun <E : StoredEntity> MongoCollection<E>.txDelete(session: ClientSession, id: String): Boolean {
    return deleteOne(session, mongoById(id)).deletedCount == 1L
}
