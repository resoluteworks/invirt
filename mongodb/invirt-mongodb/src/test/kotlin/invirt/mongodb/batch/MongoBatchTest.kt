package invirt.mongodb.batch

import com.mongodb.client.model.InsertManyOptions
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.VersionedDocument
import invirt.mongodb.mongoNow
import invirt.testMongoAtlas
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class MongoBatchTest : StringSpec() {

    private val mongo = testMongoAtlas()

    init {

        "batch with large number of documents" {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = spyk(mongo.database.getCollection<TestDocument>(collectionName))

            collection.withBatch(10) { batch ->
                repeat(18) {
                    batch.add(TestDocument())
                }
                batch.addAll((1..30).map { TestDocument() })
            }

            verify(exactly = 5) { collection.insertMany(any(), any<InsertManyOptions>()) }
            collection.countDocuments() shouldBe 48
        }

        "batch with exact number of documents" {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = spyk(mongo.database.getCollection<TestDocument>(collectionName))

            collection.withBatch(10) { batch ->
                repeat(10) {
                    batch.add(TestDocument())
                }
            }

            verify(exactly = 1) { collection.insertMany(any(), any<InsertManyOptions>()) }
            collection.countDocuments() shouldBe 10
        }

        "batch with small number of documents" {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = spyk(mongo.database.getCollection<TestDocument>(collectionName))

            collection.withBatch(10) { batch ->
                repeat(7) {
                    batch.add(TestDocument())
                }
            }

            verify(exactly = 1) { collection.insertMany(any(), any<InsertManyOptions>()) }
            collection.countDocuments() shouldBe 7
        }
    }
}
