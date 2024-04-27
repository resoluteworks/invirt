package invirt.data.mongodb

import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.test.randomDatabase
import invirt.test.testMongoClient
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import java.time.Instant
import java.util.*

class QueryTest : StringSpec() {

    private val mongoDb = testMongoClient().randomDatabase()

    init {
        "no results" {
            @CollectionName("query-no-results-test")
            data class Entity(
                @Indexed val type: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            collection.insertMany((0 until 10).map { Entity("company") })
            collection.insertMany((0 until 10).map { Entity("individual") })

            val result = collection.query(Entity::type.eq("something-else"), Page(0, 10))
            result.count shouldBe 0
            result.records.size shouldBe 0
        }

        "paged result" {
            @CollectionName("query-paged-result-test")
            data class Entity(
                @Indexed val type: String,
                @Indexed val index: Int,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            val companyCount = 95
            collection.insertMany((0 until companyCount).map { Entity("company", it) })
            collection.insertMany((0 until 100).map { Entity("individual", it) })

            val result1 = collection.query(Entity::type.eq("company"), Page(0, 10))
            result1.count shouldBe companyCount
            result1.sort.shouldBeEmpty()
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(Entity::type.eq("company"), Page(10, 10))
            result2.count shouldBe companyCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe (10..19).toList()

            val result3 = collection.query(Entity::type.eq("company"), Page(90, 10))
            result3.count shouldBe companyCount
            result3.records.size shouldBe 5
            result3.records.map { it.type }.toSet() shouldBe setOf("company")
            result3.records.map { it.index } shouldBe (90..94).toList()
        }

        "sort" {
            @CollectionName("query-sort-test")
            data class Entity(
                @Indexed val type: String,
                @Indexed val index: Int,

                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            val docCount = 95
            collection.insertMany((0 until docCount).map { Entity("company", it) })

            val result1 = collection.query(Entity::type.eq("company"), Page(0, 10), 1000, Entity::index.sortAsc())
            result1.sort shouldBe listOf(Sort("index", SortOrder.ASC))
            result1.count shouldBe docCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 = collection.query(Entity::type.eq("company"), Page(0, 10), 1000, Entity::index.sortDesc())
            result2.sort shouldBe listOf(Sort("index", SortOrder.DESC))
            result2.count shouldBe docCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe ((docCount - 1) downTo 85).toList()
        }

        "sort with multiple fields" {
            @CollectionName("query-sort-multiple-sorts-test")
            data class Entity(
                @Indexed val type: String,
                @Indexed val index: Int,

                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            val docCount = 95
            collection.insertMany((1..docCount).map { Entity("company", it) })
            collection.insertMany((1..docCount).map { Entity("individual", it) })

            val result = collection.query(null, Page(0, 10), 1000, Entity::index.sortAsc(), Entity::type.sortDesc())
            result.sort shouldBe listOf(Sort("index", SortOrder.ASC), Sort("type", SortOrder.DESC))
            result.count shouldBe docCount * 2
            result.records.size shouldBe 10
            result.records.count { it.type == "company" } shouldBe 5
            result.records.count { it.type == "individual" } shouldBe 5

            // Even records are "person"
            result.records.map { it.type }.filterIndexed { index, _ -> index % 2 == 0 }.toSet() shouldBe setOf("individual")

            // Odd records are "company"
            result.records.map { it.type }.filterIndexed { index, _ -> index % 2 == 1 }.toSet() shouldBe setOf("company")
        }

        "search query" {
            @CollectionName("query-search-query-test")
            data class Entity(
                @Indexed val type: String,
                @Indexed val index: Int,

                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            data class SimpleMongoQuery(
                override val filter: Bson?,
                override val page: Page,
                override val sort: List<Sort>
            ) : MongoQuery

            val collection = mongoDb.getEntityCollection<Entity>()
            val companyCount = 95
            val individualCount = 100
            collection.insertMany((0 until companyCount).map { Entity("company", it) })
            collection.insertMany((0 until individualCount).map { Entity("individual", it) })

            val result1 =
                collection.query(
                    SimpleMongoQuery(
                        Entity::type.eq("company"),
                        Page(0, 10),
                        listOf(Entity::index.sortAsc())
                    )
                )
            result1.sort shouldBe listOf(Sort("index", SortOrder.ASC))
            result1.count shouldBe companyCount
            result1.records.size shouldBe 10
            result1.records.map { it.type }.toSet() shouldBe setOf("company")
            result1.records.map { it.index } shouldBe (0..9).toList()

            val result2 =
                collection.query(
                    SimpleMongoQuery(
                        Entity::type.eq("company"),
                        Page(0, 10),
                        listOf(Entity::index.sortDesc())
                    )
                )
            result2.sort shouldBe listOf(Sort("index", SortOrder.DESC))
            result2.count shouldBe companyCount
            result2.records.size shouldBe 10
            result2.records.map { it.type }.toSet() shouldBe setOf("company")
            result2.records.map { it.index } shouldBe (94 downTo 85).toList()
        }

        "no filter should return all documents" {
            @CollectionName("query-no-filter-should-return-all-docs-test")
            data class Entity(
                @Indexed val type: String,

                @BsonId override val id: String = UUID.randomUUID().toString(),
                override val createdAt: Instant = Instant.now(),
                override var updatedAt: Instant = Instant.now()
            ) : TimestampedEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            val companyCount = 95
            val individualCount = 100
            collection.insertMany((0 until companyCount).map { Entity("company") })
            collection.insertMany((0 until individualCount).map { Entity("individual") })
            val result = collection.query(null, Page(0, 10))
            result.count shouldBe companyCount + individualCount
        }

        "text search" {
            @CollectionName("query-text-search-test")
            data class Entity(
                @TextIndexed
                val name: String,

                @TextIndexed
                val description: String,

                @BsonId override val id: String = UUID.randomUUID().toString(),
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            val doc1 = Entity("dogs", "dogs barking")
            val doc2 = Entity("both", "cats and dogs")
            collection.save(doc1)
            collection.save(doc2)

            collection.query(textSearch("dogs")).records.sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.query(textSearch("dog")).records.sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.query(textSearch("barking")).records shouldBe listOf(doc1)
            collection.query(textSearch("cat")).records shouldBe listOf(doc2)
        }

        "collection.query with maxDocuments" {
            @CollectionName("query-collection-query-with-maxDocuments")
            data class Entity(
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            collection.insertMany((0 until 100).map { Entity() })

            val result = collection.query(null, Page(0, 10), 30)
            result.count shouldBe 30
            result.records.size shouldBe 10
        }

        "MongoQuery with maxDocuments" {
            @CollectionName("query-MongoQuery-with-maxDocuments")
            data class Entity(
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            collection.insertMany((0 until 100).map { Entity() })
            data class SimpleMongoQuery(
                override val filter: Bson?,
                override val page: Page,
                override val maxDocuments: Int,
                override val sort: List<Sort> = emptyList()
            ) : MongoQuery

            val result = collection.query(SimpleMongoQuery(filter = null, page = Page(0, 10), maxDocuments = 25))
            result.count shouldBe 25
            result.records.size shouldBe 10
        }
    }
}
