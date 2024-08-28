package invirt.mongodb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Facet
import invirt.data.Page
import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.randomTestCollection
import invirt.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId

class AggregatesTest : StringSpec() {

    private val mongo = testMongo()

    init {

        "paged aggregate - basics" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()

            val documents = (1..100).map { TestDocument(it) }
            collection.insertMany(documents)

            collection.pagedAggregate(
                pipeline = listOf(
                    Aggregates.match(TestDocument::index.mongoLte(50)),
                    Aggregates.sort(TestDocument::index.sortAsc().mongoSort())
                ),
                page = Page(0, 10)
            ).recordsPage.records.map { it.index } shouldContainExactly (1..10).toList()

            collection.pagedAggregate(
                pipeline = listOf(
                    Aggregates.match(TestDocument::index.mongoLte(50)),
                    Aggregates.sort(TestDocument::index.sortDesc().mongoSort())
                ),
                page = Page(0, 10)
            ).recordsPage.records.map { it.index } shouldContainExactly (50 downTo 41).toList()
        }

        "paged aggregate - with custom facets" {
            data class TestDocument(
                val index: Int,
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()

            val documents = (0 until 100).map {
                val type = if (it % 2 == 0) "person" else "company"
                TestDocument(it + 1, type)
            }
            collection.insertMany(documents)

            val result = collection.pagedAggregate(
                pipeline = listOf(
                    Aggregates.match(TestDocument::index.mongoLte(50)),
                    Aggregates.sort(TestDocument::index.sortAsc().mongoSort())
                ),
                page = Page(0, 10),
                facets = listOf(
                    Facet(
                        "count-companies",
                        listOf(Aggregates.match(TestDocument::type.mongoEq("company")), Aggregates.count())
                    )
                )
            )

            result.recordsPage.records.map { it.index } shouldContainExactly (1..10).toList()
            (result.rawResult["count-companies"] as List<Document>)[0]["count"] shouldBe 25
        }
    }
}
