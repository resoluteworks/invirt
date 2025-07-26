package invirt.mongodb

import com.mongodb.client.model.Indexes
import invirt.data.doesntExist
import invirt.data.exists
import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import invirt.data.withinGeoBounds
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.LocalDate

class FiltersTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "gt, lt, gte, lte" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (1..100).map { TestDocument(it) }
            collection.insertMany(documents)
            collection.find(TestDocument::index.mongoGt(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (11..100).toList()
            collection.find("index".mongoGt(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (11..100).toList()
            collection.find(TestDocument::index.mongoGte(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (10..100).toList()
            collection.find("index".mongoGte(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (10..100).toList()
            collection.find(TestDocument::index.mongoGte(0)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..100).toList()
            collection.find("index".mongoGte(0)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..100).toList()
            collection.find(TestDocument::index.mongoLt(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..53).toList()
            collection.find("index".mongoLt(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..53).toList()
            collection.find(TestDocument::index.mongoLte(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..54).toList()
            collection.find("index".mongoLte(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..54).toList()
        }

        "eq" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (1..100).map { TestDocument(it) }
            collection.insertMany(documents)
            collection.find(TestDocument::index.mongoEq(10)).toList().map { it.index } shouldBe listOf(10)
            collection.find("index".mongoEq(10)).toList().map { it.index } shouldBe listOf(10)
        }

        "exists and doesntExist"
        {
            data class Name(val firstName: String?, val lastName: String?)
            data class TestDocument(
                val name: Name?,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val e1 = collection.insert(TestDocument(null)).id
            val e2 = collection.insert(TestDocument(Name(firstName = "John", lastName = null))).id
            val e3 = collection.insert(TestDocument(Name(firstName = "John", lastName = "Smith"))).id
            val e4 = collection.insert(TestDocument(Name(firstName = null, lastName = "Smith"))).id

            collection.find("name".exists().mongoFilter()).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(e2, e3, e4)
            collection.find("name".doesntExist().mongoFilter()).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(e1)
            collection.find("name.lastName".exists().mongoFilter()).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(e3, e4)

            collection.find("name.lastName".doesntExist().mongoFilter()).toList().map { it.id } shouldContainExactlyInAnyOrder
                listOf(e1, e2)

            collection.find("name.firstName".exists().mongoFilter()).toList().map { it.id } shouldContainExactlyInAnyOrder
                listOf(e2, e3)

            collection.find("name.firstName".doesntExist().mongoFilter()).toList().map { it.id } shouldContainExactlyInAnyOrder
                listOf(e1, e4)
        }

        "in"
        {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val ids = mutableListOf<String>()
            repeat(4) {
                val document = TestDocument(it % 2)
                collection.insertOne(document)
                ids.add(document.id)
            }

            collection.find(TestDocument::index.mongoIn(0, 1)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(TestDocument::index.mongoIn(setOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(TestDocument::index.mongoIn(listOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids

            collection.find("index".mongoIn(0, 1)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find("index".mongoIn(setOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find("index".mongoIn(listOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids

            collection.find(TestDocument::index.mongoIn(0)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[0], ids[2])
            collection.find("index".mongoIn(0)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[0], ids[2])

            collection.find(TestDocument::index.mongoIn(1)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[1], ids[3])
            collection.find("index".mongoIn(1)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[1], ids[3])

            // Error states
            val message = "Values for mongoIn cannot be empty"
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                TestDocument::index.mongoIn(emptySet())
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                TestDocument::index.mongoIn(emptyList())
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                TestDocument::index.mongoIn()
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                "index".mongoIn(emptySet())
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                "index".mongoIn(emptyList())
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                "index".mongoIn()
            }
        }

        "inYear"
        {
            data class TestDocument(
                val index: Int,
                val date: LocalDate,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany(
                listOf(
                    TestDocument(1, LocalDate.of(2024, 2, 3)),
                    TestDocument(2, LocalDate.of(2024, 2, 3)),
                    TestDocument(3, LocalDate.of(2025, 2, 3)),
                    TestDocument(4, LocalDate.of(2025, 2, 3)),
                    TestDocument(5, LocalDate.of(2025, 2, 3)),
                    TestDocument(6, LocalDate.of(2026, 2, 3))
                )
            )

            collection.find(TestDocument::date.inYear(2024)).toList().map { it.index } shouldContainExactlyInAnyOrder listOf(1, 2)
            collection.find(TestDocument::date.inYear(2025)).toList().map { it.index } shouldContainExactlyInAnyOrder listOf(3, 4, 5)
            collection.find(TestDocument::date.inYear(2026)).toList().map { it.index } shouldContainExactlyInAnyOrder listOf(6)
        }

        "byId"
        {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val ids = mutableListOf<String>()
            repeat(4) {
                val document = TestDocument()
                collection.insertOne(document)
                ids.add(document.id)
            }

            collection.find(mongoById(ids[0])).toList().map { it.id } shouldBe listOf(ids[0])
            collection.find(mongoById(ids[3])).toList().map { it.id } shouldBe listOf(ids[3])
        }

        "byIds"
        {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val ids = mutableListOf<String>()
            repeat(4) {
                val document = TestDocument()
                collection.insertOne(document)
                ids.add(document.id)
            }

            collection.find(mongoByIds(ids)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(mongoByIds(*ids.toTypedArray())).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(mongoByIds(ids.sorted().subList(0, 2))).toList().map { it.id } shouldContainExactlyInAnyOrder ids.sorted()
                .subList(0, 2)
        }

        "text search"
        {
            data class TestDocument(
                val name: String,
                val description: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.createIndex(Indexes.compoundIndex(listOf("name", "description").map { Indexes.text(it) }))

            val doc1 = TestDocument("dogs", "dogs barking")
            val doc2 = TestDocument("both", "cats and dogs")
            collection.insertOne(doc1)
            collection.insertOne(doc2)

            collection.find(mongoTextSearch("dogs")).toList().sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.find(mongoTextSearch("dog")).toList().sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.find(mongoTextSearch("barking")).toList() shouldBe listOf(doc1)
            collection.find(mongoTextSearch("cat")).toList() shouldBe listOf(doc2)
        }

        "withinGeoBounds"
        {
            data class TestDocument(
                val location: GeoLocation,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.createIndex(Indexes.geo2dsphere("location.lngLat"))

            val e1 = collection.insert(TestDocument(GeoLocation(lng = -7.0, lat = 49.1)))
            val e2 = collection.insert(TestDocument(GeoLocation(lng = 10.5, lat = 23.0)))
            val e3 = collection.insert(TestDocument(GeoLocation(lng = 22.0, lat = -10.1)))

            collection.find(
                "location.lngLat".withinGeoBounds(
                    GeoBoundingBox(
                        southWest = GeoLocation(lng = -10.0, lat = -30.0),
                        northEast = GeoLocation(lng = 25.1, lat = 55.0)
                    )
                ).mongoFilter()
            ).toList() shouldContainExactlyInAnyOrder listOf(e1, e2, e3)

            collection.find(
                TestDocument::location.mongoGeoBounds(
                    GeoBoundingBox(
                        southWest = GeoLocation(lng = -10.0, lat = -30.0),
                        northEast = GeoLocation(lng = 25.1, lat = 55.0)
                    )
                )
            ).toList() shouldContainExactlyInAnyOrder listOf(e1, e2, e3)

            collection.find(
                "location.lngLat".withinGeoBounds(
                    GeoBoundingBox(
                        southWest = GeoLocation(lng = -10.0, lat = -30.0),
                        northEast = GeoLocation(lng = 17.1, lat = 55.0)
                    )
                ).mongoFilter()
            ).toList() shouldContainExactlyInAnyOrder listOf(e1, e2)

            collection.find(
                "location.lngLat".withinGeoBounds(
                    GeoBoundingBox(
                        southWest = GeoLocation(lng = -1.0, lat = -30.0),
                        northEast = GeoLocation(lng = 25.1, lat = 55.0)
                    )
                ).mongoFilter()
            ).toList() shouldContainExactlyInAnyOrder listOf(e2, e3)
        }
    }
}
