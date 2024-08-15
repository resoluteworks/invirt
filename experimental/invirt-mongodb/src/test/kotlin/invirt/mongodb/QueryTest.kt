package invirt.mongodb

import invirt.data.Page
import invirt.data.Sort
import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import java.time.Instant

class QueryTest : StringSpec() {

    private val mongo = testMongo()
    private val database = mongo.database

    init {
        "default args" {
            @MongoCollection("query-default-args")
            data class Entity(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            collection.insertMany((0 until 34).map { Entity("company") })
            collection.insertMany((0 until 23).map { Entity("individual") })

            val result = collection.query()
            result.totalCount shouldBe 57
            result.records.size shouldBe 10
        }

        "no results" {
            @MongoCollection("query-no-results-test")
            data class Entity(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            collection.insertMany((0 until 10).map { Entity("company") })
            collection.insertMany((0 until 10).map { Entity("individual") })

            val result = collection.query(Entity::type.mongoEq("something-else"), Page(0, 10))
            result.totalCount shouldBe 0
            result.records.size shouldBe 0
        }

        "paged result" {
            @MongoCollection("query-paged-result-test")
            data class Entity(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            val companyCount = 95
            collection.insertMany((0 until companyCount).map { Entity("company", it) })
            collection.insertMany((0 until 100).map { Entity("individual", it) })

            val result1 = collection.query(Entity::type.mongoEq("company"), Page(0, 10))
            result1.totalCount shouldBe companyCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(Entity::type.mongoEq("company"), Page(10, 10))
            result2.totalCount shouldBe companyCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe (10..19).toList()

            val result3 = collection.query(Entity::type.mongoEq("company"), Page(90, 10))
            result3.totalCount shouldBe companyCount
            result3.records.size shouldBe 5
            result3.records.map { it.type }.toSet() shouldBe setOf("company")
            result3.records.map { it.index } shouldBe (90..94).toList()
        }

        "sort" {
            @MongoCollection("query-sort-test")
            data class Entity(
                val type: String,
                val index: Int,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            val docCount = 95
            collection.insertMany((0 until docCount).map { Entity("company", it) })

            val result1 = collection.query(Entity::type.mongoEq("company"), Page(0, 10), 1000, Entity::index.sortAsc())
            result1.totalCount shouldBe docCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(Entity::type.mongoEq("company"), Page(0, 10), 1000, Entity::index.sortDesc())
            result2.totalCount shouldBe docCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe ((docCount - 1) downTo 85).toList()
        }

        "sort with multiple fields" {
            @MongoCollection("query-sort-multiple-sorts-test")
            data class Entity(
                val type: String,
                val index: Int,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            val docCount = 95
            collection.insertMany((1..docCount).map { Entity("company", it) })
            collection.insertMany((1..docCount).map { Entity("individual", it) })

            val result = collection.query(null, Page(0, 10), 1000, Entity::index.sortAsc(), Entity::type.sortDesc())
            result.totalCount shouldBe docCount * 2
            result.records.size shouldBe 10
            result.records.count { it.type == "company" } shouldBe 5
            result.records.count { it.type == "individual" } shouldBe 5

            // Even records are "person"
            result.records.map { it.type }.filterIndexed { index, _ -> index % 2 == 0 }.toSet() shouldBe setOf("individual")

            // Odd records are "company"
            result.records.map { it.type }.filterIndexed { index, _ -> index % 2 == 1 }.toSet() shouldBe setOf("company")
        }

        "search query" {
            @MongoCollection("query-search-query-test")
            data class Entity(
                val type: String,
                val index: Int,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            data class SimpleMongoQuery(override val filter: Bson?, override val page: Page, override val sort: List<Sort>) : MongoQuery

            val collection = database.collection<Entity>()
            val companyCount = 95
            val individualCount = 100
            collection.insertMany((0 until companyCount).map { Entity("company", it) })
            collection.insertMany((0 until individualCount).map { Entity("individual", it) })

            val result1 =
                collection.query(
                    SimpleMongoQuery(
                        Entity::type.mongoEq("company"),
                        Page(0, 10),
                        listOf(Entity::index.sortAsc())
                    )
                )
            result1.totalCount shouldBe companyCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(
                SimpleMongoQuery(
                    Entity::type.mongoEq("company"),
                    Page(0, 10),
                    listOf(Entity::index.sortDesc())
                )
            )
            result2.totalCount shouldBe companyCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe (94 downTo 85).toList()
        }

        "no filter should return all documents" {
            @MongoCollection("query-no-filter-should-return-all-docs-test")
            data class Entity(
                val type: String,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            val companyCount = 95
            val individualCount = 100
            collection.insertMany((0 until companyCount).map { Entity("company") })
            collection.insertMany((0 until individualCount).map { Entity("individual") })
            val result = collection.query(null, Page(0, 10))
            result.totalCount shouldBe companyCount + individualCount
        }

        "text search" {
            @MongoCollection("query-text-search-test")
            data class Entity(
                val name: String,
                val description: String,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            collection.createIndexes {
                text(Entity::name, Entity::description)
            }

            val doc1 = Entity("dogs", "dogs barking")
            val doc2 = Entity("both", "cats and dogs")
            collection.save(doc1)
            collection.save(doc2)

            collection.query(mongoTextSearch("dogs")).records.sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.query(mongoTextSearch("dog")).records.sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.query(mongoTextSearch("barking")).records shouldBe listOf(doc1)
            collection.query(mongoTextSearch("cat")).records shouldBe listOf(doc2)
        }

        "collection.query with maxDocuments" {
            @MongoCollection("query-collection-query-with-maxDocuments")
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            collection.insertMany((0 until 100).map { Entity() })

            val result = collection.query(null, Page(0, 10), 30)
            result.totalCount shouldBe 30
            result.records.size shouldBe 10
        }

        "MongoQuery with maxDocuments" {
            @MongoCollection("query-MongoQuery-with-maxDocuments")
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = database.collection<Entity>()
            collection.insertMany((0 until 100).map { Entity() })
            data class SimpleMongoQuery(
                override val filter: Bson?,
                override val page: Page,
                override val maxDocuments: Int,
                override val sort: List<Sort> = emptyList()
            ) : MongoQuery

            val result = collection.query(SimpleMongoQuery(filter = null, page = Page(0, 10), maxDocuments = 25))
            result.totalCount shouldBe 25
            result.records.size shouldBe 10
        }
    }
}
