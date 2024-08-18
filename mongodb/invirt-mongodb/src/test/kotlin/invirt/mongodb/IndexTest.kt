package invirt.mongodb

import invirt.data.sortAsc
import invirt.test.shouldHaveAscIndex
import invirt.test.shouldHaveDescIndex
import invirt.test.shouldHaveTextIndex
import invirt.testMongo
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
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
            collection.createIndices {
                timestampedIndices()
                asc(Person::age)
                asc("gender.name")
                desc(Person::lastName)
                asc(Person::indexedAndTextIndexed)
                text("address.city", "firstName", "indexedAndTextIndexed")
            }

            collection shouldHaveAscIndex "age"
            collection shouldHaveAscIndex "gender.name"
            collection shouldHaveDescIndex "lastName"
            collection shouldHaveAscIndex "version"
            collection shouldHaveDescIndex "createdAt"
            collection shouldHaveDescIndex "updatedAt"
            collection.shouldHaveTextIndex("address.city", "firstName", "indexedAndTextIndexed")
        }

        "can only add text index once" {
            data class Person(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Person>(collectionName)

            shouldThrowWithMessage<IllegalStateException>("Text index already added for this collection") {
                collection.createIndices {
                    text(Person::name)
                    text("name")
                }
            }
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
            collection.createIndices {
                desc(TestDocument::childId)
            }

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
            collection.createIndices {
                asc(Person::age)
                asc(Person::name)
                asc(Person::lastName, caseInsensitive = true)
                desc("firstName")
                desc(Person::address, caseInsensitive = true)
                desc("city", caseInsensitive = true)
            }
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
            collection.createIndices {
                asc(Person::name)
            }

            collection.insert(Person("B"))
            collection.insert(Person("a"))
            collection.find().sort(Person::name.sortAsc())
                .caseInsensitive()
                .map { it.name }.toList() shouldBe listOf("a", "B")
        }
    }

    private fun List<Document>.indexForField(field: String): Document = find { (it["key"] as Document)[field] != null } as Document
}
