package invirt.mongodb

import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.shouldBeNextUpdateOf
import invirt.mongo.test.shouldBeSameDocument
import invirt.mongo.test.shouldBeUpdateOf
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class CollectionTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "findOne" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertOne(TestDocument(1))
            collection.insertOne(TestDocument(2))
            collection.insertOne(TestDocument(3))
            collection.insertOne(TestDocument(4))

            collection.findOne(TestDocument::index.mongoEq(2))!!.index shouldBe 2
            collection.findOne(TestDocument::index.mongoEq(5)) shouldBe null
            shouldThrowWithMessage<IllegalStateException>("Multiple MongoDB documents found for filter ${TestDocument::index.mongoGt(2)}") {
                collection.findOne(TestDocument::index.mongoGt(2))
            }
        }

        "findFirst" {
            data class TestDoc(
                val index: Int,
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collection = mongo.randomTestCollection<TestDoc>()
            val doc1 = collection.insert(TestDoc(1, "person"))
            val doc2 = collection.insert(TestDoc(2, "person"))
            val doc3 = collection.insert(TestDoc(3, "company"))
            val doc4 = collection.insert(TestDoc(4, "company"))

            collection.findFirst(TestDoc::type.mongoEq("person"), TestDoc::index.sortAsc().mongoSort()) shouldBeSameDocument doc1
            collection.findFirst(TestDoc::type.mongoEq("person"), TestDoc::index.sortDesc().mongoSort()) shouldBeSameDocument doc2
            collection.findFirst(TestDoc::type.mongoEq("company"), TestDoc::index.sortDesc().mongoSort()) shouldBeSameDocument doc4
            collection.findFirst(TestDoc::type.mongoEq("nothing"), TestDoc::index.sortDesc().mongoSort()) shouldBe null
        }

        "get" {
            data class TestDocument(
                val name: String = uuid7(),
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val doc1 = TestDocument()
            val doc2 = TestDocument()
            collection.insertOne(doc1)
            collection.insertOne(doc2)

            collection.get(doc1.id) shouldBeSameDocument doc1
            collection.get(doc2.id) shouldBeSameDocument doc2
            collection.get("test") shouldBe null
        }

        "delete" {
            data class TestDocument(
                val name: String = uuid7(),
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val id = collection.insertOne(TestDocument()).insertedId!!.asString().value
            collection.countDocuments() shouldBe 1

            collection.delete(id) shouldBe true
            collection.countDocuments() shouldBe 0
            collection.get(id) shouldBe null
            collection.delete(id) shouldBe false
        }

        "insert" {
            data class TestDocument(
                val name: String = uuid7(),
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collection = mongo.randomTestCollection<TestDocument>()

            val doc = collection.insert(TestDocument())

            collection.countDocuments() shouldBe 1
            val createdDoc = collection.get(doc.id)!!
            createdDoc.version shouldBe 1
            createdDoc.name shouldBe doc.name
        }

        "update" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val doc = TestDocument("document")
            val id = collection.insert(doc).id

            val updatedDoc1 = collection.update(collection.get(id)!!)
            collection.countDocuments() shouldBe 1
            collection.get(id) shouldBe updatedDoc1
            updatedDoc1 shouldBeUpdateOf doc
            updatedDoc1 shouldBeNextUpdateOf doc

            val updatedDoc2 = collection.update(collection.get(id)!!.copy(type = "folder"))
            collection.countDocuments() shouldBe 1
            collection.get(id) shouldBe updatedDoc2
            updatedDoc2 shouldBeUpdateOf doc
            updatedDoc2 shouldBeUpdateOf updatedDoc1
            updatedDoc2 shouldBeNextUpdateOf updatedDoc1
            updatedDoc2.type shouldBe "folder"
        }

        "update - optimistic lock failure" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val v1 = collection.insert(TestDocument("document"))

            v1.version shouldBe 1

            // This update will succeed and update the version to 2
            collection.update(v1.copy(type = "folder"))

            // Attempting to update v1 should fail
            shouldThrow<VersionConflictException> {
                collection.update(v1.copy(type = "container"))
            }

            // The initial update to "folder" would not be overwrritten by the failed update attempt
            val lastVersion = collection.get(v1.id)!!
            lastVersion.type shouldBe "folder"
            lastVersion.version shouldBe 2
        }

        "update document - optimistic lock failure with successful retry" {
            data class TestDocument(
                val email: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val v1 = collection.insert(TestDocument("johnsmith1@test.com"))

            // This update will succeed and increase the version to 2
            collection.update(v1.copy(email = "johnsmith2@test.com"))
            collection.get(v1.id)!!.email shouldBe "johnsmith2@test.com"

            // Attempt to update with a patch when the optimistic lock fails
            collection.update(v1.copy(email = "johnsmith3@test.com")) {
                it.copy(email = "johnsmith3@test.com")
            }

            val lastVersion = collection.get(v1.id)!!
            lastVersion.email shouldBe "johnsmith3@test.com"
            lastVersion.version shouldBe 3
        }

        "update document - optimistic lock failure with failed retry" {
            data class TestDocument(
                val email: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val v1 = collection.insert(TestDocument("jonhsmith1@test.com"))
            val v2 = collection.update(v1.copy(email = "jonhsmith2@test.com"))

            shouldThrow<VersionConflictException> {
                collection.update(v1.copy(email = "jonhsmithX@test.com")) {
                    // Fake an update by another thread/process
                    collection.update(v2.copy(email = "jonhsmith3@test.com"))
                    it.copy(email = "jonhsmithX@test.com")
                }
            }

            // The update to "jonhsmithX@test.com" must've failed and the update from the
            // other thread/process is the last version
            val lastVersion = collection.get(v1.id)!!
            lastVersion.email shouldBe "jonhsmith3@test.com"
            lastVersion.version shouldBe 3
        }

        "id consistency" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val doc = TestDocument("Mary")
            collection.insert(doc)
            collection.get(doc.id)!!.id shouldBe doc.id
        }

        "findByIds" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val doc1 = collection.insert(TestDocument("Mary"))
            val doc2 = collection.insert(TestDocument("John"))
            val doc3 = collection.insert(TestDocument("Jane"))

            collection.findByIds(doc1.id, doc2.id, doc3.id).map { it.id } shouldBe listOf(doc1.id, doc2.id, doc3.id)
            collection.findByIds(doc1.id, doc2.id, "test").map { it.id } shouldBe listOf(doc1.id, doc2.id)
            collection.findByIds().size shouldBe 0
        }
    }
}
