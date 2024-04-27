package invirt.data.mongodb

import invirt.test.randomDatabase
import invirt.test.testMongoClient
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant
import java.util.*

class MongoEntityTest : StringSpec() {

    private val mongoDatabase = testMongoClient().randomDatabase()

    init {
        "collectionName" {
            @CollectionName("users")
            data class User(@BsonId override val id: String) : MongoEntity
            collectionName<User>() shouldBe "users"

            data class Story(@BsonId override val id: String) : MongoEntity
            shouldThrowWithMessage<IllegalStateException>("Class ${Story::class} doesn't have an @CollectionName annotation") {
                collectionName<Story>()
            }
        }

        "MongoCollection.save" {
            @CollectionName("mongo-entity-save-test")
            data class Entity(
                val name: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDatabase.getCollection<Entity>("mongo-entity-save-test")
            val entity = Entity("John")
            collection.save(entity)
            collection.get(entity.id) shouldBe entity
        }

        "MongoCollection.save(Timestamped)" {
            @CollectionName("mongo-entity-save-timestamped-test")
            data class Entity(
                val name: String,
                override val createdAt: Instant = Instant.now(),
                override var updatedAt: Instant = Instant.now(),
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : TimestampedEntity

            val collection = mongoDatabase.getCollection<Entity>("mongo-entity-save-timestamped-test")
            val entity = Entity("John")
            collection.save(entity)
            val updated = collection.save(collection.get(entity.id)!!)

            updated.createdAt.toEpochMilli() shouldBe entity.createdAt.toEpochMilli()
            updated.updatedAt.isAfter(entity.updatedAt) shouldBe true
        }

        "MongoDatabase.collection<MongoEntity>" {
            @CollectionName("mongo-database-get-mongo-entity-collection-test")
            data class Entity(
                val name: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDatabase.collection<Entity>()
            val entity = Entity("Mary")
            collection.save(entity)
            mongoDatabase.getCollection<Entity>("mongo-database-get-mongo-entity-collection-test").get(entity.id) shouldBe entity
        }

        "id consistency" {
            @CollectionName("mongo-database-id-consistency-test")
            data class Entity(
                val name: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDatabase.collection<Entity>()
            val entity = Entity("Mary")
            collection.save(entity)
            collection.get(entity.id)!!.id shouldBe entity.id
        }
    }
}
