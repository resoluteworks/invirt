package invirt.data.mongodb

import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.reflect.full.findAnnotation

interface MongoEntity {
    val id: String
}

interface TimestampedEntity : MongoEntity {
    @Indexed
    val createdAt: Instant

    @Indexed
    var updatedAt: Instant
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CollectionName(val name: String)

inline fun <reified T : MongoEntity> collectionName(): String {
    val annotation = T::class.findAnnotation<CollectionName>()
        ?: throw IllegalStateException("Class ${T::class} doesn't have an @CollectionName annotation")
    return annotation.name
}

fun <T : MongoEntity> MongoCollection<T>.save(entity: T): T {
    if (entity is TimestampedEntity) {
        entity.updatedAt = mongoNow()
    }
    replaceOne(byId(entity.id), entity, ReplaceOptions().upsert(true))
    return entity
}

inline fun <reified T : MongoEntity> MongoDatabase.collection(): MongoCollection<T> {
    return getCollection<T>(collectionName<T>())
}

/**
 * MongoDB only support millisecond precision
 */
fun mongoNow(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)
