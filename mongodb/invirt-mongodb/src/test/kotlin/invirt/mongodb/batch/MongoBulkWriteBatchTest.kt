package invirt.mongodb.batch

import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.InsertOneModel
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

class MongoBulkWriteBatchTest : StringSpec() {

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

            collection.withBulkWriteBatch(10) { batch ->
                repeat(18) {
                    batch.add(InsertOneModel(TestDocument()))
                }
                batch.addAll((1..30).map { InsertOneModel(TestDocument()) })
            }

            verify(exactly = 5) { collection.bulkWrite(any(), any<BulkWriteOptions>()) }
            collection.countDocuments() shouldBe 48
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

            collection.withBulkWriteBatch(10) { batch ->
                repeat(7) {
                    batch.add(InsertOneModel(TestDocument()))
                }
            }

            verify(exactly = 1) { collection.bulkWrite(any(), any<BulkWriteOptions>()) }
            collection.countDocuments() shouldBe 7
        }
    }
}
