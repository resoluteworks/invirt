package invirt.mongodb

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import invirt.data.sortAsc
import invirt.mongo.test.shouldHaveAscIndex
import invirt.mongo.test.shouldHaveDescIndex
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class DocumentIndicesTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "VersionedDocument.versionIndex is ascending on version" {
            val index = VersionedDocument.versionIndex()
            index.keys.toBsonDocument() shouldBe Indexes.ascending("version").toBsonDocument()
        }

        "TimestampedDocument indices are descending on createdAt and updatedAt" {
            val expected = listOf(Indexes.descending("createdAt"), Indexes.descending("updatedAt")).map { it.toBsonDocument() }
            TimestampedDocument.timestampIndicesList().map { it.keys.toBsonDocument() } shouldContainExactly expected
            TimestampedDocument.timestampIndices().map { it.keys.toBsonDocument() } shouldContainExactly expected
            TimestampedDocument.allIndices().map { it.keys.toBsonDocument() } shouldContainExactly
                listOf(Indexes.ascending("version").toBsonDocument()) + expected
        }

        "TimestampedDocument.allIndices creates the version and timestamp indices" {
            data class Person(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collection = mongo.database.getCollection<Person>(uuid7())
            collection.createIndexes(TimestampedDocument.allIndices().toList())

            collection shouldHaveAscIndex "version"
            collection shouldHaveDescIndex "createdAt"
            collection shouldHaveDescIndex "updatedAt"
        }

        "caseInsensitive collation on an index and a sort" {
            data class Person(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.database.getCollection<Person>(uuid7())
            collection.createIndex(Indexes.ascending(Person::name.name), IndexOptions().collation(caseInsensitive()))

            val collation = collection.listIndexes().toList()
                .first { (it["key"] as Document)["name"] != null }["collation"] as Document
            collation["locale"] shouldBe "en"
            collation["strength"] shouldBe 3
            collation["caseLevel"] shouldBe false

            collection.insert(Person("B"))
            collection.insert(Person("a"))
            collection.find().sort(Person::name.sortAsc())
                .collation(caseInsensitive())
                .map { it.name }.toList() shouldBe listOf("a", "B")
        }
    }
}
