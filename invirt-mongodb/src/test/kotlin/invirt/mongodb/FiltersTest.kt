package invirt.mongodb

import com.mongodb.client.model.Indexes
import invirt.test.randomCollection
import invirt.test.testMongo
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
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
            collection.find(Entity::index.mongoGt(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (11..100).toList()
            collection.find(Entity::index.mongoGte(10)).toList().map { it.index } shouldContainExactlyInAnyOrder (10..100).toList()
            collection.find(Entity::index.mongoGte(0)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..100).toList()
            collection.find(Entity::index.mongoLt(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..53).toList()
            collection.find(Entity::index.mongoLte(54)).toList().map { it.index } shouldContainExactlyInAnyOrder (1..54).toList()
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

            collection.find(Entity::index.mongoIn(0, 1)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(Entity::index.mongoIn(setOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(Entity::index.mongoIn(listOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids

            collection.find("index".mongoIn(0, 1)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find("index".mongoIn(setOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find("index".mongoIn(listOf(0, 1))).toList().map { it.id } shouldContainExactlyInAnyOrder ids

            collection.find(Entity::index.mongoIn(0)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[0], ids[2])
            collection.find("index".mongoIn(0)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[0], ids[2])

            collection.find(Entity::index.mongoIn(1)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[1], ids[3])
            collection.find("index".mongoIn(1)).toList().map { it.id } shouldContainExactlyInAnyOrder listOf(ids[1], ids[3])

            // Error states
            val message = "Values for mongoIn cannot be empty"
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                Entity::index.mongoIn(emptySet())
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                Entity::index.mongoIn(emptyList())
            }
            shouldThrowWithMessage<IllegalArgumentException>(message) {
                Entity::index.mongoIn()
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

            collection.find(mongoById(ids[0])).toList().map { it.id } shouldBe listOf(ids[0])
            collection.find(mongoById(ids[3])).toList().map { it.id } shouldBe listOf(ids[3])
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

            collection.find(mongoByIds(ids)).toList().map { it.id } shouldContainExactlyInAnyOrder ids
            collection.find(mongoByIds(ids.sorted().subList(0, 2))).toList().map { it.id } shouldContainExactlyInAnyOrder ids.sorted()
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

            collection.find(mongoTextSearch("dogs")).toList().sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.find(mongoTextSearch("dog")).toList().sortedBy { it.id } shouldBe listOf(doc1, doc2).sortedBy { it.id }
            collection.find(mongoTextSearch("barking")).toList() shouldBe listOf(doc1)
            collection.find(mongoTextSearch("cat")).toList() shouldBe listOf(doc2)
        }
    }
}
