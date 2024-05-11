package invirt.mongodb

import invirt.data.sortAsc
import invirt.test.shouldHaveAscIndex
import invirt.test.shouldHaveDescIndex
import invirt.test.shouldHaveTextIndex
import invirt.test.testMongo
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

            data class Entity(
                @Indexed
                val age: Int,

                @Indexed(fields = ["gender.name"])
                val gender: Gender,

                @TextIndexed
                val firstName: String,

                @TextIndexed(fields = ["address.city"])
                val address: Address,

                @Indexed
                @TextIndexed
                val indexedAndTextIndexed: String,

                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val database = mongo.database

            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()

            collection shouldHaveAscIndex "age"
            collection shouldHaveAscIndex "gender.name"
            collection shouldHaveAscIndex "version"
            collection shouldHaveAscIndex "createdAt"
            collection shouldHaveAscIndex "updatedAt"

            collection.listIndexes().forEach {
                println(it)
            }
            collection.shouldHaveTextIndex("address.city", "firstName", "indexedAndTextIndexed")
        }

        "create indexes - explicit ASC" {
            data class Entity(
                @Indexed(order = Indexed.Order.ASC) val parentId: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()

            collection shouldHaveAscIndex "parentId"
        }

        "create indexes - explicit DESC" {
            data class Entity(
                @Indexed(order = Indexed.Order.DESC) val childId: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()

            collection shouldHaveDescIndex "childId"
        }

        "case insensitive only on strings" {
            data class Entity(
                @Indexed val age: Int,
                @Indexed val name: String,
                @Indexed(caseInsensitive = false) val lastName: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collectionName = uuid7()
            val database = mongo.database
            database.createCollection(collectionName)
            val collection = database.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()
            val indexes = collection.listIndexes().toList()
            indexes.indexForField("age")["collation"] shouldBe null
            indexes.indexForField("lastName")["collation"] shouldBe null
            indexes.indexForField("name")["collation"] shouldNotBe null
        }

        "case insensitive sort" {
            data class Entity(
                @Indexed val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val database = mongo.database
            val collectionName = uuid7()
            database.createCollection(collectionName)
            val collection = database.getCollection<Entity>(collectionName)
            collection.createEntityIndexes()

            collection.save(Entity("B"))
            collection.save(Entity("a"))
            collection.find().sort(Entity::name.sortAsc())
                .caseInsensitive()
                .map { it.name }.toList() shouldBe listOf("a", "B")
        }
    }

    private fun List<Document>.indexForField(field: String): Document {
        return find { (it["key"] as Document)[field] != null } as Document
    }
}
