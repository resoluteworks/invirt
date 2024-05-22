package invirt.mongodb

import invirt.test.*
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class CollectionTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "findOne" {
            data class Entity(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            collection.insertOne(Entity(1))
            collection.insertOne(Entity(2))
            collection.insertOne(Entity(3))
            collection.insertOne(Entity(4))

            collection.findOne(Entity::index.mongoEq(2))!!.index shouldBe 2
            collection.findOne(Entity::index.mongoEq(5)) shouldBe null
            shouldThrowWithMessage<IllegalStateException>("More than one document found for filter ${Entity::index.mongoGt(2)}") {
                collection.findOne(Entity::index.mongoGt(2))
            }
        }

        "get" {
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            val entity1 = Entity()
            val entity2 = Entity()
            collection.insertOne(entity1)
            collection.insertOne(entity2)

            collection.get(entity1.id) shouldBeSameEntity entity1
            collection.get(entity2.id) shouldBeSameEntity entity2
            collection.get("test") shouldBe null
        }

        "delete" {
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            val id = collection.insertOne(Entity()).insertedId!!.asString().value
            collection.countDocuments() shouldBe 1

            collection.delete(id) shouldBe true
            collection.countDocuments() shouldBe 0
            collection.get(id) shouldBe null
            collection.delete(id) shouldBe false
        }

        "save" {
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()

            val entity1 = collection.save(Entity())
            collection.countDocuments() shouldBe 1

            val entity2 = collection.save(collection.get(entity1.id)!!)
            collection.countDocuments() shouldBe 1

            entity2 shouldBeSameEntity entity1
            entity2 shouldBeNextUpdateOf entity1
        }

        "collectionName" {
            @MongoCollection("users")
            data class User(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity
            collectionName<User>() shouldBe "users"

            data class Story(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity
            shouldThrowWithMessage<IllegalStateException>("Class ${Story::class} doesn't have an @CollectionName annotation") {
                collectionName<Story>()
            }
        }

        "id consistency" {
            @MongoCollection("mongo-database-id-consistency-test")
            data class Entity(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.collection<Entity>()
            val entity = Entity("Mary")
            collection.save(entity)
            collection.get(entity.id)!!.id shouldBe entity.id
        }
    }
}
