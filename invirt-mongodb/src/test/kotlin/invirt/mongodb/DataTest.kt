package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import invirt.data.*
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

        "Filter.mongoFilter()" {
            FieldFilter.eq("type", "person").mongoFilter() shouldBe Filters.eq("type", "person")
            FieldFilter.ne("status", "open").mongoFilter() shouldBe Filters.ne("status", "open")
            FieldFilter.gt("age", 37).mongoFilter() shouldBe Filters.gt("age", 37)
            FieldFilter.gte("age", 18).mongoFilter() shouldBe Filters.gte("age", 18)
            FieldFilter.lt("age", 55).mongoFilter() shouldBe Filters.lt("age", 55)
            FieldFilter.lte("age", 98).mongoFilter() shouldBe Filters.lte("age", 98)

            CompoundFilter.and(
                CompoundFilter.or(FieldFilter.eq("status", "married"), FieldFilter.eq("status", "single")),
                CompoundFilter.and(FieldFilter.gte("age", 18), FieldFilter.lt("age", 100)),
                FieldFilter.eq("type", "person")
            ).mongoFilter() shouldBe Filters.and(
                Filters.or(Filters.eq("status", "married"), Filters.eq("status", "single")),
                Filters.and(Filters.gte("age", 18), Filters.lt("age", 100)),
                Filters.eq("type", "person")
            )
        }
    }
}
