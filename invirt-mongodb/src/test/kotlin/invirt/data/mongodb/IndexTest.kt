package invirt.data.mongodb

import invirt.data.sortAsc
import invirt.test.randomDatabase
import invirt.test.testMongoClient
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant
import java.util.*

class IndexTest : StringSpec() {

    private val mongoDatabase = testMongoClient().randomDatabase()

    init {
        "create indexes" {
            data class Entity(
                @Indexed
                val age: Int,

                @TextIndexed
                val name: String,

                @Indexed
                @TextIndexed
                val indexedAndTextIndexed: String,

                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collectionName = UUID.randomUUID().toString()
            mongoDatabase.createCollection(collectionName)
            val collection = mongoDatabase.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()
            val indexes = collection.listIndexes().toList()
            indexes.indexForField("age").sortOrder("age") shouldBe 1
            indexes.firstOrNull {
                (it["key"] as Document)["_fts"] == "text" && (it["name"] == "indexedAndTextIndexed_text_name_text")
            } shouldNotBe null
        }

        "create indexes - explicit ASC" {
            data class Entity(
                @Indexed(order = Indexed.Order.ASC) val parentId: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collectionName = UUID.randomUUID().toString()
            mongoDatabase.createCollection(collectionName)
            val collection = mongoDatabase.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()
            collection.listIndexes().toList().indexForField("parentId").sortOrder("parentId") shouldBe 1
        }

        "create indexes - explicit DESC" {
            data class Entity(
                @Indexed(order = Indexed.Order.DESC) val childId: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collectionName = UUID.randomUUID().toString()
            mongoDatabase.createCollection(collectionName)
            val collection = mongoDatabase.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()
            collection.listIndexes().toList().indexForField("childId").sortOrder("childId") shouldBe -1
        }

        "create indexes - timestamped" {
            data class Entity(
                @Indexed
                val ownerId: String,
                @BsonId override val id: String = UUID.randomUUID().toString(),
                override val createdAt: Instant = Instant.now(),
                override var updatedAt: Instant = Instant.now()
            ) : TimestampedEntity

            val collectionName = UUID.randomUUID().toString()
            mongoDatabase.createCollection(collectionName)
            val collection = mongoDatabase.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()
            val indexes = collection.listIndexes().toList()
            indexes.indexForField("ownerId").sortOrder("ownerId") shouldBe 1
            indexes.indexForField("createdAt").sortOrder("createdAt") shouldBe 1
            indexes.indexForField("updatedAt").sortOrder("updatedAt") shouldBe 1
        }

        "case insensitive only on strings" {
            data class Entity(
                @Indexed val age: Int,
                @Indexed val name: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collectionName = UUID.randomUUID().toString()
            mongoDatabase.createCollection(collectionName)
            val collection = mongoDatabase.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()
            val indexes = collection.listIndexes().toList()
            indexes.indexForField("age")["collation"] shouldBe null
            indexes.indexForField("name")["collation"] shouldNotBe null
        }

        "case insensitive sort" {
            data class Entity(
                @Indexed
                val name: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val collectionName = UUID.randomUUID().toString()
            mongoDatabase.createCollection(collectionName)
            val collection = mongoDatabase.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()

            collection.save(Entity("B"))
            collection.save(Entity("a"))
            collection.find().sort(Entity::name.sortAsc())
                .caseInsensitive()
                .map { it.name }.toList() shouldBe listOf("a", "B")
        }
    }

    private fun Document.sortOrder(field: String): Int? {
        return (this["key"] as Document)[field] as Int?
    }

    private fun List<Document>.indexForField(field: String): Document {
        return find { (it["key"] as Document)[field] != null } as Document
    }
}
