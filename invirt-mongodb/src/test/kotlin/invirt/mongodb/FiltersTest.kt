package invirt.mongodb

import com.mongodb.client.model.Indexes
import invirt.test.randomCollection
import invirt.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant
import java.time.LocalDate

class FiltersTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "gt, lt, gte, lte" {
            data class Entity(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            repeat(100) {
                collection.insertOne(Entity(it + 1))
            }
            collection.find(Entity::index.gt(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (11..100).toList()
            collection.find(Entity::index.gte(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (10..100).toList()
            collection.find(Entity::index.gte(0)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..100).toList()
            collection.find(Entity::index.lt(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..53).toList()
            collection.find(Entity::index.lte(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..54).toList()
        }

        "in" {
            data class Entity(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            val ids = mutableListOf<String>()
            repeat(4) {
                val document = Entity(it % 2)
                collection.insertOne(document)
                ids.add(document.id)
            }

            collection.find(Entity::index.`in`(0, 1)!!).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(Entity::index.`in`(setOf(0, 1))!!).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(Entity::index.`in`(listOf(0, 1))!!).toList().map { it.id } shouldContainExactlyInAnyOrder ids

            collection.find("index".`in`(0, 1)!!).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find("index".`in`(setOf(0, 1))!!).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find("index".`in`(listOf(0, 1))!!).toList().map { it.id } shouldContainExactlyInAnyOrder ids

            collection.find(Entity::index.`in`(0)!!).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[0], ids[2])
            collection.find("index".`in`(0)!!).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[0], ids[2])

            collection.find(Entity::index.`in`(1)!!).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[1], ids[3])
            collection.find("index".`in`(1)!!).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[1], ids[3])

            // Null values
            Entity::index.`in`(emptySet()) shouldBe null
            Entity::index.`in`(emptyList()) shouldBe null
            Entity::index.`in`() shouldBe null
            "index".`in`(emptySet()) shouldBe null
            "index".`in`(emptyList()) shouldBe null
            "index".`in`() shouldBe null
        }

        "inYear" {
            data class Entity(
                val index: Int,
                val date: LocalDate,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            collection.insertMany(
                listOf(
                    Entity(1, LocalDate.of(2024, 2, 3)),
                    Entity(2, LocalDate.of(2024, 2, 3)),
                    Entity(3, LocalDate.of(2025, 2, 3)),
                    Entity(4, LocalDate.of(2025, 2, 3)),
                    Entity(5, LocalDate.of(2025, 2, 3)),
                    Entity(6, LocalDate.of(2026, 2, 3))
                )
            )

            collection.find(Entity::date.inYear(2024)).toList().map { it.index } shouldContainExactlyInAnyOrder listOf(1, 2)
            collection.find(Entity::date.inYear(2025)).toList().map { it.index } shouldContainExactlyInAnyOrder listOf(3, 4, 5)
            collection.find(Entity::date.inYear(2026)).toList().map { it.index } shouldContainExactlyInAnyOrder listOf(6)
        }

        "byId" {
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            val ids = mutableListOf<String>()
            repeat(4) {
                val document = Entity()
                collection.insertOne(document)
                ids.add(document.id)
            }

            collection.find(byId(ids[0])).toList().map { it.id } shouldBe listOf(ids[0])
            collection.find(byId(ids[3])).toList().map { it.id } shouldBe listOf(ids[3])
        }

        "byIds" {
            data class Entity(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            val ids = mutableListOf<String>()
            repeat(4) {
                val document = Entity()
                collection.insertOne(document)
                ids.add(document.id)
            }

            collection.find(byIds(ids)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(byIds(ids.sorted().subList(0, 2))).toList().map { it.id } shouldContainExactlyInAnyOrder ids.sorted()
                .subList(0, 2)
        }

        "text search" {
            data class Entity(
                val name: String,
                val description: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            collection.createIndex(Indexes.compoundIndex(listOf("name", "description").map { Indexes.text(it) }))

            val doc1 = Entity("dogs", "dogs barking")
            val doc2 = Entity("both", "cats and dogs")
            collection.insertOne(doc1)
            collection.insertOne(doc2)

            collection.find(textSearch("dogs")).toList().sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.find(textSearch("dog")).toList().sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.find(textSearch("barking")).toList() shouldBe listOf(doc1)
            collection.find(textSearch("cat")).toList() shouldBe listOf(doc2)
        }
    }
}
