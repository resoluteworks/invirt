package invirt.mongodb

import invirt.test.randomCollection
import invirt.test.testMongo
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class TransactionTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "tx failure" {
            data class Entity(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()

            shouldThrow<IllegalStateException> {
                mongo.runInTransaction { session ->
                    collection.txSave(session, Entity("Test"))
                    throw IllegalStateException("Unlucky")
                }
            }
            collection.countDocuments() shouldBe 0
        }

        "tx success" {
            data class Entity(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            val id1 = collection.save(Entity("One")).id
            val id2 = collection.save(Entity("Two")).id
            val id3 = mongo.runInTransaction { session ->
                collection.txDelete(session, id2)
                collection.txDelete(session, "123") // Shouldn't cause a problem as it doesn't exist
                collection.txSave(session, collection.get(id1)!!.copy(name = "1"))
                collection.txSave(session, Entity("3")).id
            }

            collection.countDocuments() shouldBe 2
            collection.get(id1)!!.name shouldBe "1"
            collection.get(id3)!!.name shouldBe "3"
        }
    }
}
