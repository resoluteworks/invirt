package invirt.mongo.test

import invirt.mongodb.TimestampedDocument
import invirt.mongodb.VersionedDocument
import invirt.mongodb.get
import invirt.mongodb.insert
import invirt.mongodb.mongoNow
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant
import java.time.temporal.ChronoUnit

class CollectionTestUtilsTest : StringSpec() {

    init {
        "spy collection should override the original collection's behavior" {
            data class TestDocument(
                val index: Int,
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val mongo = testMongo().spyCollection<TestDocument>("spy-collection-test") {
                every { it.find() } throws RuntimeException("I cannot find anything!")
            }

            shouldThrowWithMessage<RuntimeException>("I cannot find anything!") {
                mongo.database.getCollection<TestDocument>("spy-collection-test").find()
            }
        }

        "setCreatedAt backdates createdAt only, leaving version and updatedAt untouched" {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val mongo = testMongo()
            val collection = mongo.database.getCollection<TestDocument>(uuid7())
            val inserted = collection.insert(TestDocument())
            val backdated = mongoNow().minus(30, ChronoUnit.DAYS)

            collection.setCreatedAt(inserted.id, backdated)

            val reloaded = collection.get(inserted.id)!!
            reloaded.createdAt shouldBe backdated
            reloaded.version shouldBe inserted.version
            reloaded.updatedAt shouldBe inserted.updatedAt
        }
    }
}
