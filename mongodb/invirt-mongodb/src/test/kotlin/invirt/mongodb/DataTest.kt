package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.geojson.Polygon
import com.mongodb.client.model.geojson.Position
import invirt.data.DataFilter
import invirt.data.Page
import invirt.data.Sort
import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId

class DataTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "FindIterable.page" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (0 until 100).map { TestDocument(it) }
            collection.insertMany(documents)
            collection.find().page(Page(0, 10)).toList().map { it.index } shouldContainExactlyInAnyOrder (0..9).toList()
            collection.find().page(Page(40, 20)).toList().map { it.index } shouldContainExactlyInAnyOrder (40..59).toList()
        }

        "Sort.mongoSort" {
            Sort.asc("name").mongoSort() shouldBe Sorts.ascending("name")
            Sort.desc("name").mongoSort() shouldBe Sorts.descending("name")
            emptyList<Sort>().mongoSort() shouldBe emptyList()
        }

        "Sort.mongoSort multiple values" {
            listOf(Sort.asc("name"), Sort.desc("age")).mongoSort() shouldContainExactly listOf(
                Sorts.ascending("name"),
                Sorts.descending("age")
            )
        }

        "FindIterable.sort" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (0 until 100).map { TestDocument(it) }
            collection.insertMany(documents)
            collection.find().sort(Sort.asc("index")).toList().map { it.index } shouldBe (0..99).toList()
            collection.find().sort(Sort.desc("index")).toList().map { it.index } shouldBe (99 downTo 0).toList()
            collection.find().sort().toList().map { it.index } shouldContainExactlyInAnyOrder (0..99).toList()
        }

        "Filter.mongoFilter()" {
            DataFilter.Field.eq("type", "person").mongoFilter() shouldBe Filters.eq("type", "person")
            DataFilter.Field.ne("status", "open").mongoFilter() shouldBe Filters.ne("status", "open")
            DataFilter.Field.gt("age", 37).mongoFilter() shouldBe Filters.gt("age", 37)
            DataFilter.Field.gte("age", 18).mongoFilter() shouldBe Filters.gte("age", 18)
            DataFilter.Field.lt("age", 55).mongoFilter() shouldBe Filters.lt("age", 55)
            DataFilter.Field.lte("age", 98).mongoFilter() shouldBe Filters.lte("age", 98)

            DataFilter.Field.withingGeoBounds(
                "location",
                GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
            ).mongoFilter() shouldBe Filters.geoWithin(
                "location",
                Polygon(
                    listOf(
                        Position(-8.596867, 51.348611),
                        Position(-8.596867, 56.435911),
                        Position(1.950008, 56.435911),
                        Position(1.950008, 51.348611),
                        Position(-8.596867, 51.348611)
                    )
                )
            )

            DataFilter.Compound.and(
                DataFilter.Compound.or(DataFilter.Field.eq("status", "married"), DataFilter.Field.eq("status", "single")),
                DataFilter.Compound.and(DataFilter.Field.gte("age", 18), DataFilter.Field.lt("age", 100)),
                DataFilter.Field.eq("type", "person")
            ).mongoFilter() shouldBe Filters.and(
                Filters.or(Filters.eq("status", "married"), Filters.eq("status", "single")),
                Filters.and(Filters.gte("age", 18), Filters.lt("age", 100)),
                Filters.eq("type", "person")
            )
        }
    }
}
