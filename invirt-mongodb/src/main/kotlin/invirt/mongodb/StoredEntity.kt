package invirt.mongodb

import java.time.Instant
import kotlin.reflect.full.findAnnotation

interface StoredEntity {
    val id: String
    var version: Long
    val createdAt: Instant
    var updatedAt: Instant
}

inline fun <reified Entity : StoredEntity> collectionName(): String {
    val annotation = Entity::class.findAnnotation<MongoCollection>()
        ?: throw IllegalStateException("Class ${Entity::class} doesn't have an @CollectionName annotation")
    return annotation.name
}

fun <Entity : StoredEntity> Entity.updated(): Entity {
    version++
    updatedAt = mongoNow()
    return this
}
