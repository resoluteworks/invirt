package invirt.mongodb

import invirt.data.Page
import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId

class QueryTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "default args" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((0 until 34).map { TestDocument("company") })
            collection.insertMany((0 until 23).map { TestDocument("individual") })

            val result = collection.query()
            result.totalCount shouldBe 57
            result.records.size shouldBe 10
        }

        "no results" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((0 until 10).map { TestDocument("folder") })
            collection.insertMany((0 until 10).map { TestDocument("document") })

            val result = collection.query(TestDocument::type.mongoEq("something-else"), Page(0, 10))
            result.totalCount shouldBe 0
            result.records.size shouldBe 0
        }

        "paged result" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val companyCount = 95
            collection.insertMany((0 until companyCount).map { TestDocument("company", it) })
            collection.insertMany((0 until 100).map { TestDocument("individual", it) })

            val result1 = collection.query(TestDocument::type.mongoEq("company"), Page(0, 10))
            result1.totalCount shouldBe companyCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(TestDocument::type.mongoEq("company"), Page(10, 10))
            result2.totalCount shouldBe companyCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe (10..19).toList()

            val result3 = collection.query(TestDocument::type.mongoEq("company"), Page(90, 10))
            result3.totalCount shouldBe companyCount
            result3.records.size shouldBe 5
            result3.records.map { it.type }.toSet() shouldBe setOf("company")
            result3.records.map { it.index } shouldBe (90..94).toList()
        }

        "sort" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val docCount = 95
            collection.insertMany((0 until docCount).map { TestDocument("company", it) })

            val result1 = collection.query(
                filter = TestDocument::type.mongoEq("company"),
                page = Page(0, 10),
                sort = listOf(TestDocument::index.sortAsc().mongoSort())
            )

            result1.totalCount shouldBe docCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(
                filter = TestDocument::type.mongoEq("company"),
                page = Page(0, 10),
                sort = listOf(TestDocument::index.sortDesc().mongoSort())
            )
            result2.totalCount shouldBe docCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe ((docCount - 1) downTo 85).toList()
        }

        "sort case insensitive" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.createIndices(
                TestDocument::name.asc { caseInsensitive() }
            )
            collection.insertMany(
                listOf(
                    TestDocument("Apple"),
                    TestDocument("Banana"),
                    TestDocument("almond")
                )
            )

            // The default won't be sorted
            collection.query(sort = listOf(TestDocument::name.sortAsc().mongoSort()))
                .records.map { it.name } shouldContainExactly listOf("Apple", "Banana", "almond")

            // The case-insensitive index will sort correctly
            collection.query(sort = listOf(TestDocument::name.sortAsc().mongoSort())) { caseInsensitive() }
                .records.map { it.name } shouldContainExactly listOf("almond", "Apple", "Banana")
        }

        "sort with multiple fields" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val docCount = 95
            collection.insertMany((1..docCount).map { TestDocument("company", it) })
            collection.insertMany((1..docCount).map { TestDocument("individual", it) })

            val result = collection.query(
                filter = null,
                page = Page(0, 10),
                sort = listOf(TestDocument::index.sortAsc().mongoSort(), TestDocument::type.sortDesc().mongoSort())
            )
            result.totalCount shouldBe docCount * 2
            result.records.size shouldBe 10
            result.records.count { it.type == "company" } shouldBe 5
            result.records.count { it.type == "individual" } shouldBe 5

            // Even records are "person"
            result.records.map { it.type }.filterIndexed { index, _ -> index % 2 == 0 }.toSet() shouldBe setOf("individual")

            // Odd records are "company"
            result.records.map { it.type }.filterIndexed { index, _ -> index % 2 == 1 }.toSet() shouldBe setOf("company")
        }

        "no filter should return all documents" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documentCount = 95
            val folderCount = 100
            collection.insertMany((0 until documentCount).map { TestDocument("document") })
            collection.insertMany((0 until folderCount).map { TestDocument("folder") })
            val result = collection.query(null, Page(0, 10))
            result.totalCount shouldBe documentCount + folderCount
        }

        "collection.query with maxDocuments" {
            data class TestDocument(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((0 until 100).map { TestDocument() })

            val result = collection.query(null, Page(0, 10), maxDocuments = 30)
            result.totalCount shouldBe 30
            result.records.size shouldBe 10
        }
    }
}
