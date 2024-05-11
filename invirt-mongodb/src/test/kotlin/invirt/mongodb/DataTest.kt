package invirt.mongodb

import com.mongodb.client.model.Sorts
import invirt.data.Page
import invirt.data.Sort
import invirt.test.randomCollection
import invirt.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

class DataTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "FindIterable.page" {
            data class Entity(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            repeat(100) {
                collection.insertOne(Entity(it))
            }
            collection.find().page(Page(0, 10)).toList().map { it.index } shouldContainExactlyInAnyOrder (0..9).toList()
            collection.find().page(Page(40, 20)).toList().map { it.index } shouldContainExactlyInAnyOrder (40..59).toList()
        }

        "Sort.mongoSort" {
            Sort.asc("name").mongoSort() shouldBe Sorts.ascending("name")
            Sort.desc("name").mongoSort() shouldBe Sorts.descending("name")
            emptyList<Sort>().mongoSort() shouldBe null
        }

        "Sort.mongoSort multiple values" {
            listOf(Sort.asc("name"), Sort.desc("age")).mongoSort() shouldBe Sorts.orderBy(Sorts.ascending("name"), Sorts.descending("age"))
        }

        "FindIterable.sort" {
            data class Entity(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override val createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : StoredEntity

            val collection = mongo.database.randomCollection<Entity>()
            repeat(100) {
                collection.insertOne(Entity(it))
            }
            collection.find().sort(Sort.asc("index")).toList().map { it.index } shouldBe (0..99).toList()
            collection.find().sort(Sort.desc("index")).toList().map { it.index } shouldBe (99 downTo 0).toList()
            collection.find().sort().toList().map { it.index } shouldContainExactlyInAnyOrder (0..99).toList()
        }
    }
}
