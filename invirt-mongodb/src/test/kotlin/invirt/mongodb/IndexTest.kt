package invirt.mongodb

import invirt.data.sortAsc
import invirt.mongo.test.shouldHaveAscIndex
import invirt.mongo.test.shouldHaveDescIndex
import invirt.mongo.test.shouldHaveTextIndex
import invirt.mongo.test.shouldHaveUniqueIndex
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class IndexTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "create indexes" {
            data class Gender(val name: String)
            data class Address(val city: String)

            data class Person(
                val age: Int,
                val gender: Gender,
                val firstName: String,
                val lastName: String,
                val address: Address,
                val indexedAndTextIndexed: String,
                val sent: Instant,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val database = mongo.database

            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Person>(collectionName)
            collection.createIndices(
                Person::age.asc(),
                "gender.name".asc { unique(true) },
                Person::lastName.desc(),
                Person::indexedAndTextIndexed.asc(),
                textIndex("address.city", "firstName", "indexedAndTextIndexed"),
                *TimestampedDocument.allIndices()
            )

            collection shouldHaveAscIndex "age"
            collection shouldHaveAscIndex "gender.name"
            collection shouldHaveUniqueIndex "gender.name"
            collection shouldHaveDescIndex "lastName"
            collection shouldHaveAscIndex "version"
            collection shouldHaveDescIndex "createdAt"
            collection shouldHaveDescIndex "updatedAt"
            collection.shouldHaveTextIndex("address.city", "firstName", "indexedAndTextIndexed")
        }

        "create indexes - desc" {
            data class TestDocument(
                val childId: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<TestDocument>(collectionName)
            collection.createIndices(
                TestDocument::childId.desc()
            )

            collection shouldHaveDescIndex "childId"
        }

        "case insensitive" {
            data class Person(
                val age: Int,
                val name: String,
                val lastName: String,
                val firstName: String,
                val address: String,
                val city: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            val database = mongo.database
            database.createCollection(collectionName)
            val collection = database.getCollection<Person>(collectionName)
            collection.createIndices(
                Person::age.asc(),
                Person::name.asc(),
                Person::lastName.asc { caseInsensitive() },
                "firstName".desc(),
                Person::address.desc { caseInsensitive() },
                "city".desc { caseInsensitive() }
            )
            val indexes = collection.listIndexes().toList()
            indexes.indexForField("age")["collation"] shouldBe null
            indexes.indexForField("name")["collation"] shouldBe null
            indexes.indexForField("lastName")["collation"] shouldNotBe null
            indexes.indexForField("firstName")["collation"] shouldBe null
            indexes.indexForField("address")["collation"] shouldNotBe null
            indexes.indexForField("city")["collation"] shouldNotBe null
        }

        "case insensitive sort" {
            data class Person(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Person>(collectionName)
            collection.createIndices(
                Person::name.asc()
            )

            collection.insert(Person("B"))
            collection.insert(Person("a"))
            collection.find().sort(Person::name.sortAsc())
                .collation(caseInsensitive())
                .map { it.name }.toList() shouldBe listOf("a", "B")
        }

        "compound indexes" {
            data class Run(
                val organisationId: String,
                val draftId: String,
                val descriptionHash: String,
                val status: String,
                val topic: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Run>(collectionName)
            collection.createIndices(
                // Same direction, so the whole index is one call
                ascIndex(Run::organisationId, Run::descriptionHash, Run::status),
                // A single field is the same index asc() builds
                ascIndex(Run::draftId),
                // Field names, for a path no property names
                ascIndex("nested.one", "nested.two"),
                // Options apply to the compound index as a whole
                ascIndex(Run::organisationId, Run::draftId) {
                    unique(true).partialFilterExpression(Run::status.mongoEq("PENDING"))
                },
                // Mixed directions need the keys spelled out
                compoundIndex(Run::organisationId.ascKey(), Run::createdAt.descKey()),
                compoundIndex(Run::topic.ascKey(), "nested.three".descKey(), Run::updatedAt.descKey()) { unique(true) }
            )

            val indexes = collection.listIndexes().toList()

            indexes.keyForFields("organisationId", "descriptionHash", "status") shouldBe listOf(1, 1, 1)
            indexes.keyForFields("draftId") shouldBe listOf(1)
            indexes.keyForFields("nested.one", "nested.two") shouldBe listOf(1, 1)

            indexes.keyForFields("organisationId", "draftId") shouldBe listOf(1, 1)
            val partial = indexes.indexForFields("organisationId", "draftId")
            partial["unique"] shouldBe true
            partial["partialFilterExpression"] shouldBe Document("status", "PENDING")

            indexes.keyForFields("organisationId", "createdAt") shouldBe listOf(1, -1)
            indexes.keyForFields("topic", "nested.three", "updatedAt") shouldBe listOf(1, -1, -1)
            indexes.indexForFields("topic", "nested.three", "updatedAt")["unique"] shouldBe true
        }
    }

    private fun List<Document>.indexForField(field: String): Document = find { (it["key"] as Document)[field] != null } as Document

    /** The index whose keys are exactly [fields], in that order. */
    private fun List<Document>.indexForFields(vararg fields: String): Document =
        first { (it["key"] as Document).keys.toList() == fields.toList() }

    /** The directions of the index whose keys are exactly [fields], in that order. */
    private fun List<Document>.keyForFields(vararg fields: String): List<Any> =
        (indexForFields(*fields)["key"] as Document).values.toList()
}
