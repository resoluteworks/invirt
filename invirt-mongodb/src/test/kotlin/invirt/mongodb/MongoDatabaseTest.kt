package invirt.mongodb

import invirt.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class MongoDatabaseTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "get collection" {
            @MongoCollection("mongodb-get-collection-test")
            data class Entity(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val entity = Entity("John")
            mongo.database.collection<Entity>().save(entity)
            mongo.database.getCollection<Entity>("mongodb-get-collection-test").get(entity.id) shouldBe entity
        }
    }

    private fun Document.getIndexOrder(field: String): Int? {
        return (this["key"] as Document)[field] as Int?
    }
}
