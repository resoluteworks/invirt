package invirt.mongodb

import com.mongodb.client.model.Sorts
import invirt.data.Sort
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId

class MongoQueryTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "default args" {
            data class TestDocument(
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (0 until 34).map { TestDocument("company") }
            collection.insertMany(documents)

            val result = collection.query().find()
            result.totalCount shouldBe 34
            result.records.size shouldBe 10
        }

        "filters, pagination, sort" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (0 until 34).map { TestDocument("company", it) }
                .plus((0 until 23).map { TestDocument("individual", it) })

            collection.insertMany(documents)

            val result = collection.query()
                .filter(TestDocument::type.mongoEq("company"))
                .sort(TestDocument::index.mongoSortDesc())
                .page(0, 10)
                .find()
            result.totalCount shouldBe 34
            result.records.size shouldBe 10
            result.records.map { it.id } shouldContainExactlyInAnyOrder documents.filter { it.type == "company" }
                .sortedByDescending { it.index }
                .map { it.id }.take(10)
        }

        "query with sortAsc(String) and sortDesc(String)" {
            data class TestDocument(
                val index: Int,
                val name: String
            )

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((0 until 6).map { TestDocument(it, "name$it") })

            collection.query().sortAsc("index").find().records.map { it.index } shouldBe (0..5).toList()
            collection.query().sortDesc("index").find().records.map { it.index } shouldBe (5 downTo 0).toList()
            collection.query().sortAsc("name").find().records.map { it.name } shouldBe (0..5).map { "name$it" }
            collection.query().sortDesc("name").find().records.map { it.name } shouldBe (5 downTo 0).map { "name$it" }
        }

        "query with sortAsc(KProperty) and sortDesc(KProperty)" {
            data class TestDocument(
                val index: Int,
                val name: String
            )

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((0 until 6).map { TestDocument(it, "name$it") })

            collection.query().sortAsc(TestDocument::index).find().records.map { it.index } shouldBe (0..5).toList()
            collection.query().sortDesc(TestDocument::index).find().records.map { it.index } shouldBe (5 downTo 0).toList()
            collection.query().sortAsc(TestDocument::name).find().records.map { it.name } shouldBe (0..5).map { "name$it" }
            collection.query().sortDesc(TestDocument::name).find().records.map { it.name } shouldBe (5 downTo 0).map { "name$it" }
        }

        "query with sort(Sort)" {
            data class TestDocument(
                val index: Int,
                val name: String
            )

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany((0 until 6).map { TestDocument(it, "name$it") })

            collection.query().sort(Sort.asc("index")).find().records.map { it.index } shouldBe (0..5).toList()
            collection.query().sort(Sort.desc("index")).find().records.map { it.index } shouldBe (5 downTo 0).toList()
            collection.query().sort(Sort.asc("name")).find().records.map { it.name } shouldBe (0..5).map { "name$it" }
            collection.query().sort(Sort.desc("name")).find().records.map { it.name } shouldBe (5 downTo 0).map { "name$it" }
        }

        "query with sort(Collection<Bson>)" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = mutableListOf<TestDocument>()
            documents.add(TestDocument("company", 1))
            documents.add(TestDocument("person", 1))
            documents.add(TestDocument("company", 2))
            documents.add(TestDocument("person", 2))
            documents.add(TestDocument("company", 3))
            documents.add(TestDocument("person", 3))
            collection.insertMany(documents)

            val result = collection.query()
                .sort(listOf(Sorts.ascending("index"), Sorts.descending("type")))
                .page(0, 100)
                .find()
            result.records.map { it.id } shouldContainExactly listOf(
                documents[1].id,
                documents[0].id,
                documents[3].id,
                documents[2].id,
                documents[5].id,
                documents[4].id
            )
        }

        "query with sort(Collection<Sort>)" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = mutableListOf<TestDocument>()
            documents.add(TestDocument("company", 1))
            documents.add(TestDocument("person", 1))
            documents.add(TestDocument("company", 2))
            documents.add(TestDocument("person", 2))
            documents.add(TestDocument("company", 3))
            documents.add(TestDocument("person", 3))
            collection.insertMany(documents)

            val result = collection.query()
                .sort(listOf(Sort.asc("index"), Sort.desc("type")))
                .page(0, 100)
                .find()
            result.records.map { it.id } shouldContainExactly listOf(
                documents[1].id,
                documents[0].id,
                documents[3].id,
                documents[2].id,
                documents[5].id,
                documents[4].id
            )
        }

        "calling .sort() multiple times appends to the sort order" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = mutableListOf<TestDocument>()
            documents.add(TestDocument("company", 1))
            documents.add(TestDocument("person", 1))
            documents.add(TestDocument("company", 2))
            documents.add(TestDocument("person", 2))
            documents.add(TestDocument("company", 3))
            documents.add(TestDocument("person", 3))
            collection.insertMany(documents)

            val result = collection.query()
                .sortAsc("index")
                .sortDesc("type")
                .page(0, 100)
                .find()
            result.records.map { it.id } shouldContainExactly listOf(
                documents[1].id,
                documents[0].id,
                documents[3].id,
                documents[2].id,
                documents[5].id,
                documents[4].id
            )
        }

        "query with andFilter" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (0 until 34).map { TestDocument("company", it) }
                .plus((0 until 23).map { TestDocument("individual", it) })
            collection.insertMany(documents)

            val result = collection.query()
                .andFilter(
                    TestDocument::type.mongoEq("company"),
                    TestDocument::index.mongoLte(10)
                )
                .sortAsc("index")
                .page(0, 100)
                .find()
            result.records.map { it.id } shouldContainExactlyInAnyOrder documents.filter { it.type == "company" && it.index <= 10 }
                .sortedBy { it.index }
                .map { it.id }
        }

        "query with orFilter" {
            data class TestDocument(
                val type: String,
                val index: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val documents = (0 until 34).map { TestDocument("company", it) }
                .plus((0 until 23).map { TestDocument("individual", it) })
            collection.insertMany(documents)

            val result = collection.query()
                .orFilter(
                    TestDocument::type.mongoEq("company"),
                    TestDocument::index.mongoGte(20)
                )
                .sortAsc("index")
                .page(0, 100)
                .find()
            result.records.map { it.id } shouldContainExactlyInAnyOrder documents.filter { it.type == "company" || it.index >= 20 }
                .sortedBy { it.index }
                .map { it.id }
        }

        "collation caseInsensitive" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            collection.insertMany(
                listOf(
                    TestDocument("Banana"),
                    TestDocument("Apple"),
                    TestDocument("almond")
                )
            )

            collection.query().sortAsc("name").collation(caseInsensitive())
                .find().records.map { it.name } shouldContainExactly listOf("almond", "Apple", "Banana")
        }
    }
}
