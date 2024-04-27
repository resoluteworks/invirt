package invirt.data.mongodb

import invirt.test.randomDatabase
import invirt.test.testCollection
import invirt.test.testMongoClient
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

class CollectionTest : StringSpec() {

    private val mongoDatabase = testMongoClient().randomDatabase()

    init {
        "findOne" {
            data class Entity(
                val index: Int,
                @BsonId val id: String = UUID.randomUUID().toString()
            )

            val collection = mongoDatabase.testCollection<Entity>()
            collection.insertOne(Entity(1))
            collection.insertOne(Entity(2))
            collection.insertOne(Entity(3))
            collection.insertOne(Entity(4))

            collection.findOne(Entity::index.eq(2))!!.index shouldBe 2
            collection.findOne(Entity::index.eq(5)) shouldBe null
            shouldThrowWithMessage<IllegalStateException>("More than one document found for filter ${Entity::index.gt(2)}") {
                collection.findOne(Entity::index.gt(2))
            }
        }

        "get" {
            data class Entity(
                @BsonId val id: String = UUID.randomUUID().toString()
            )

            val collection = mongoDatabase.testCollection<Entity>()
            val entity1 = Entity()
            val entity2 = Entity()
            collection.insertOne(entity1)
            collection.insertOne(entity2)

            collection.get(entity1.id) shouldBe entity1
            collection.get(entity2.id) shouldBe entity2
            collection.get("test") shouldBe null
        }

        "delete" {
            data class Entity(
                @BsonId val id: String = UUID.randomUUID().toString()
            )

            val collection = mongoDatabase.testCollection<Entity>()
            val id = collection.insertOne(Entity()).insertedId!!.asString().value

            collection.delete(id) shouldBe true
            collection.get(id) shouldBe null
            collection.delete(id) shouldBe false
        }

        "save" {
            data class Entity(val name: String)

            val collection = mongoDatabase.testCollection<Entity>()
            val entity = Entity("John")
            val id = UUID.randomUUID().toString()

            collection.save(id, Entity("John"))
            collection.countDocuments() shouldBe 1
            collection.get(id) shouldBe Entity("John")

            // Another save overwrites the existing value
            collection.save(id, Entity("Mary"))
            collection.countDocuments() shouldBe 1
            collection.get(id) shouldBe Entity("Mary")
        }
    }
}
