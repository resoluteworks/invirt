package invirt.mongodb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Facet
import com.mongodb.client.model.Field
import invirt.data.Page
import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
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

        "paged aggregate - document stages run after skip and limit" {
            data class TestDocument(
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((1..100).map { TestDocument(it) })

            val pipeline = listOf(
                Aggregates.match(TestDocument::index.mongoLte(50)),
                Aggregates.sort(TestDocument::index.sortAsc().mongoSort())
            )

            // Omitted, the page is what it always was
            val noStages = collection.pagedAggregate(pipeline, Page(10, 10))
            noStages.recordsPage.records.map { it.index } shouldContainExactly (11..20).toList()
            noStages.recordsPage.totalCount shouldBe 50

            // An empty list of stages is the same thing
            val emptyStages = collection.pagedAggregate(pipeline, Page(10, 10), documentStages = emptyList())
            emptyStages.recordsPage.records.map { it.index } shouldContainExactly (11..20).toList()
            emptyStages.recordsPage.totalCount shouldBe 50

            // A $match in the document stages sees the page's ten rows only and leaves totalCount
            // alone, which is what "inside the documents facet, after $skip / $limit" means
            val matchStage = collection.pagedAggregate(
                pipeline = pipeline,
                page = Page(10, 10),
                documentStages = listOf(Aggregates.match(Document("index", Document("\$mod", listOf(2, 0)))))
            )
            matchStage.recordsPage.records.map { it.index } shouldContainExactly listOf(12, 14, 16, 18, 20)
            matchStage.recordsPage.totalCount shouldBe 50
        }

        "paged aggregate - document stages resolve each row of the page" {
            data class Author(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            data class Book(
                val index: Int,
                val authorId: String,
                val authorName: String? = null,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val authors = mongo.randomTestCollection<Author>()
            val books = mongo.randomTestCollection<Book>()

            val authorsByIndex = (1..100).associateWith { authors.insert(Author("Author $it")) }
            books.insertMany((1..100).map { Book(it, authorsByIndex[it]!!.id) })

            val result = books.pagedAggregate(
                pipeline = listOf(Aggregates.sort(Book::index.sortAsc().mongoSort())),
                page = Page(20, 5),
                documentStages = listOf(
                    Aggregates.lookup(authors.namespace.collectionName, Book::authorId.name, "_id", "authorLookup"),
                    Aggregates.addFields(Field(Book::authorName.name, mongoLookupField("authorLookup", Author::name.name))),
                    Aggregates.unset("authorLookup")
                )
            )

            result.recordsPage.records.map { it.index } shouldContainExactly (21..25).toList()
            result.recordsPage.totalCount shouldBe 100
            // The lookup ran for the page's five rows, not for all one hundred
            result.recordsPage.records.map { it.authorName } shouldContainExactly (21..25).map { "Author $it" }
            result.rawResult.getList("documents", Document::class.java).size shouldBe 5
        }

        "countFacet" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            Document().countFacet("missing") shouldBe 0
            Document("empty", emptyList<Document>()).countFacet("empty") shouldBe 0
            Document("counted", listOf(Document("count", 7))).countFacet("counted") shouldBe 7

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany(listOf(TestDocument("person"), TestDocument("company"), TestDocument("company")))

            val result = collection.withDocumentClass<Document>()
                .aggregate(
                    listOf(
                        Aggregates.facet(
                            Facet("people", listOf(Aggregates.match(TestDocument::type.mongoEq("person")), Aggregates.count())),
                            Facet("companies", listOf(Aggregates.match(TestDocument::type.mongoEq("company")), Aggregates.count())),
                            Facet("robots", listOf(Aggregates.match(TestDocument::type.mongoEq("robot")), Aggregates.count()))
                        )
                    )
                ).first()

            result.countFacet("people") shouldBe 1
            result.countFacet("companies") shouldBe 2
            // $count emits nothing at all when it matched nothing
            result.countFacet("robots") shouldBe 0
            result.countFacet("aliens") shouldBe 0
        }

        "facetListOf" {
            data class TestDocument(
                val index: Int,
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            Document().facetListOf<TestDocument>("missing").shouldBeEmpty()
            Document("empty", emptyList<Document>()).facetListOf<TestDocument>("empty").shouldBeEmpty()

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany(
                listOf(
                    TestDocument(1, "person"),
                    TestDocument(2, "company"),
                    TestDocument(3, "company")
                )
            )

            val result = collection.withDocumentClass<Document>()
                .aggregate(
                    listOf(
                        Aggregates.facet(
                            Facet("people", Aggregates.match(TestDocument::type.mongoEq("person"))),
                            Facet(
                                "companies",
                                listOf(
                                    Aggregates.match(TestDocument::type.mongoEq("company")),
                                    Aggregates.sort(TestDocument::index.sortAsc().mongoSort())
                                )
                            ),
                            Facet("robots", Aggregates.match(TestDocument::type.mongoEq("robot")))
                        )
                    )
                ).first()

            result.facetListOf<TestDocument>("people").map { it.index } shouldContainExactly listOf(1)
            result.facetListOf<TestDocument>("companies").map { it.index } shouldContainExactly listOf(2, 3)
            result.facetListOf<TestDocument>("robots").shouldBeEmpty()
            result.facetListOf<TestDocument>("aliens").shouldBeEmpty()
        }

        "aggregateCount" {
            data class TestDocument(
                val index: Int,
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()

            // A $count over an empty collection emits no document at all
            collection.aggregateCount(listOf(Aggregates.match(TestDocument::type.mongoEq("person")))) shouldBe 0

            collection.insertMany((1..10).map { TestDocument(it, if (it == 1) "person" else "company") })

            collection.aggregateCount(listOf(Aggregates.match(TestDocument::type.mongoEq("robot")))) shouldBe 0
            collection.aggregateCount(listOf(Aggregates.match(TestDocument::type.mongoEq("person")))) shouldBe 1
            collection.aggregateCount(listOf(Aggregates.match(TestDocument::type.mongoEq("company")))) shouldBe 9
            collection.aggregateCount(emptyList()) shouldBe 10
        }

        "countsGroupedBy" {
            data class TestDocument(
                val groupId: String,
                val status: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany(
                listOf(
                    TestDocument("group-1", "submitted"),
                    TestDocument("group-1", "submitted"),
                    TestDocument("group-1", "draft"),
                    TestDocument("group-2", "submitted"),
                    TestDocument("group-3", "draft")
                )
            )

            // Empty ids never reach the query, which would otherwise throw on an empty $in
            collection.countsGroupedBy(TestDocument::groupId, emptyList()) shouldBe emptyMap()

            collection.countsGroupedBy(TestDocument::groupId, listOf("group-1")) shouldBe mapOf("group-1" to 3)

            collection.countsGroupedBy(
                TestDocument::groupId,
                listOf("group-1", "group-2", "group-3", "group-4")
            ) shouldBe mapOf("group-1" to 3, "group-2" to 1, "group-3" to 1)

            // An id with nothing to count is absent from the map rather than zero
            collection.countsGroupedBy(TestDocument::groupId, listOf("group-4")) shouldBe emptyMap()

            collection.countsGroupedBy(
                TestDocument::groupId,
                listOf("group-1", "group-2", "group-3"),
                TestDocument::status.mongoEq("submitted")
            ) shouldBe mapOf("group-1" to 2, "group-2" to 1)
        }
    }
}
