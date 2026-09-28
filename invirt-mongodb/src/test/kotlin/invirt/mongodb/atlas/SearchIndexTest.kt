package invirt.mongodb.atlas

import com.mongodb.client.model.search.SearchOperator
import com.mongodb.kotlin.client.MongoCollection
import invirt.mongo.test.testMongoAtlas
import invirt.mongo.test.waitForSearchDocuments
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.VersionedDocument
import invirt.mongodb.insert
import invirt.mongodb.mongoNow
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.awaitility.Awaitility.await
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Duration
import java.time.Instant

class SearchIndexTest : StringSpec() {

    val mongo = testMongoAtlas()

    init {

        "create search index - autocomplete" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            val indexName = "test-index"
            collection.createSearchIndex(
                indexName,
                Document.parse(
                    """
                        {
                            "mappings": {
                                "dynamic": false,
                                "fields": {
                                    "title": {
                                        "type": "autocomplete",
                                        "analyzer": "lucene.standard",
                                        "tokenization": "edgeGram",
                                        "foldDiacritics": true
                                    }
                                }
                            }
                        }
                    """.trimIndent()
                )
            )

            collection.waitForSearchIndexReady(indexName)

            val doc1Id = collection.insert(TestDocument("Now try not to be overwhelmed by all this technology")).id
            val doc2Id = collection.insert(TestDocument("The Technocrats saw expertise as the only measure of a person")).id
            val doc3Id = collection.insert(TestDocument("Prepare yourself not only technically, but also emotionally")).id
            collection.waitForSearchDocuments("title", 3, indexName)

            fun search(text: String): List<String> {
                val searchOperator = TestDocument::title.autocomplete(text).toAggregate(indexName)
                return collection.aggregate(listOf(searchOperator)).toList().map { it.id }
            }

            search("tech") shouldContainExactlyInAnyOrder listOf(doc1Id, doc2Id, doc3Id)
            search("technology") shouldContainExactlyInAnyOrder listOf(doc1Id)
            search("techno") shouldContainExactlyInAnyOrder listOf(doc1Id, doc2Id)
            search("overwhelmed") shouldContainExactlyInAnyOrder listOf(doc1Id)
            search("emot") shouldContainExactlyInAnyOrder listOf(doc3Id)
        }

        "sort by score by default" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0,
                override var createdAt: Instant = mongoNow(),
                override var updatedAt: Instant = mongoNow()
            ) : TimestampedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            collection.createDefaultSearchIndex(
                """
                {
                    "mappings": {
                        "dynamic": false,
                        "fields": {
                            "title": {
                                "type": "string",
                                "analyzer": "stemmer"
                            }
                        }
                    },

                    "analyzers": [
                        {
                            "name": "stemmer",
                            "tokenizer": {"type": "standard"},
                            "tokenFilters": [
                                {"type": "lowercase"},
                                {"type": "porterStemming"}
                            ]
                         }
                    ]
                }
                """.trimIndent()
            )

            collection.waitForDefaultSearchIndexReady()

            // Score increases from doc1 onwards
            val doc1Id = collection.insert(TestDocument("Cats sometimes get along with dogs")).id
            val doc2Id = collection.insert(TestDocument("The cat and the dog didn't get along but the dog didn't mind")).id
            val doc3Id = collection.insert(TestDocument("A dog is a man's best friend and dogs get along with dogs and non-dogs")).id
            collection.waitForSearchDocuments("title", 3)

            collection.aggregate(listOf(TestDocument::title.textSearch("dog").toAggregate()))
                .toList()
                .map { it.id } shouldContainExactly listOf(doc3Id, doc2Id, doc1Id)

            collection.aggregate(listOf(SearchOperator.text(TestDocument::title.fieldPath(), "dogs").toAggregate()))
                .toList()
                .map { it.id } shouldContainExactly listOf(doc3Id, doc2Id, doc1Id)
        }

        "waitForDocumentInDefaultSearchIndex should succeed when searching for a field value" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            collection.createDefaultSearchIndex(
                """
                {
                    "mappings": {
                        "dynamic": false,
                        "fields": {
                            "title": {
                                "type": "string",
                                "analyzer": "lucene.standard"
                            }
                        }
                    }
                }
                """.trimIndent()
            )

            collection.waitForDefaultSearchIndexReady()

            collection.insert(TestDocument("The quick brown fox jumps over the lazy dog")).id
            shouldNotThrowAny {
                collection.waitForDocumentInDefaultSearchIndex("title", "quick", 10)
            }
        }

        "waitForDocumentInDefaultSearchIndex should succeed when searching for the _id" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            collection.createDefaultSearchIndex(
                """
                {
                    "mappings": {
                        "dynamic": false,
                        "fields": {
                            "_id": {
                                "type": "string",
                                "analyzer": "lucene.keyword"
                            }
                        }
                    }
                }
                """.trimIndent()
            )

            collection.waitForDefaultSearchIndexReady()

            val docId = collection.insert(TestDocument("The quick brown fox jumps over the lazy dog")).id
            shouldNotThrowAny {
                collection.waitForDocumentInDefaultSearchIndex("_id", docId, 10)
            }
        }

        "waitForDocumentWithIdInDefaultSearchIndex should succeed" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            collection.createDefaultSearchIndex(
                """
                {
                    "mappings": {
                        "dynamic": false,
                        "fields": {
                            "_id": {
                                "type": "string",
                                "analyzer": "lucene.keyword"
                            }
                        }
                    }
                }
                """.trimIndent()
            )

            collection.waitForDefaultSearchIndexReady()

            val docId = collection.insert(TestDocument("The quick brown fox jumps over the lazy dog")).id
            shouldNotThrowAny {
                collection.waitForDocumentWithIdInDefaultSearchIndex(docId, 10)
            }
        }

        "recreateDefaultSearchIndex should create a new index with a new definition" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            collection.createDefaultSearchIndex(
                """
                {
                    "mappings": {
                        "dynamic": false,
                        "fields": {
                            "_id": {
                                "type": "string",
                                "analyzer": "lucene.keyword"
                            }
                        }
                    }
                }
                """.trimIndent()
            )

            val docId = collection.insert(TestDocument("The quick brown fox jumps over the lazy dog")).id

            // Check the document was indexed and searching by title doesn't return anything
            collection.waitForDocumentWithIdInDefaultSearchIndex(docId, 10)
            collection.aggregate(listOf(TestDocument::title.textSearch("dog").toAggregate())).toList().shouldBeEmpty()

            // Now recreate the default index with a new definition
            collection.recreateDefaultSearchIndex(
                """
                {
                    "mappings": {
                        "dynamic": false,
                        "fields": {
                            "_id": {
                                "type": "string",
                                "analyzer": "lucene.keyword"
                            },
                            "title": {
                                "type": "string",
                                "analyzer": "lucene.standard"
                            }
                        }
                    }
                }
                """.trimIndent()
            )

            collection.waitForDefaultSearchIndexReady()

            // Searching for title should now return the document
            collection.aggregate(listOf(TestDocument::title.textSearch("dog").toAggregate()))
                .toList()
                .map { it.id } shouldContainExactly listOf(docId)
        }

        "recreateDefaultSearchIndex should replace an index that is not ready yet" {
            data class TestDocument(
                val title: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<TestDocument>(collectionName)

            // Indexing this many documents keeps the first index PENDING or BUILDING for far longer than the moment between
            // the two calls below, so it has not reached READY when it is recreated
            collection.insertMany((1..50_000).map { TestDocument("Document number $it") })

            collection.createDefaultSearchIndex(ID_INDEX_DEFINITION)
            collection.recreateDefaultSearchIndex(ID_AND_TITLE_INDEX_DEFINITION)
            collection.waitForDefaultSearchIndexReady()

            collection.mappedFields(DEFAULT_MONGO_SEARCH_INDEX) shouldBe setOf("_id", "title")
        }

        "recreateDefaultSearchIndex should create the index when none exists" {
            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<Document>(collectionName)

            collection.recreateDefaultSearchIndex(ID_AND_TITLE_INDEX_DEFINITION)
            collection.waitForDefaultSearchIndexReady()

            collection.mappedFields(DEFAULT_MONGO_SEARCH_INDEX) shouldBe setOf("_id", "title")
        }

        "recreateSearchIndex should only replace the index with the given name" {
            val collectionName = uuid7()
            mongo.database.createCollection(collectionName)
            val collection = mongo.database.getCollection<Document>(collectionName)

            collection.createSearchIndex("replaced", Document.parse(ID_INDEX_DEFINITION))
            collection.createSearchIndex("untouched", Document.parse(ID_INDEX_DEFINITION))
            collection.waitForSearchIndexReady("replaced")
            collection.waitForSearchIndexReady("untouched")
            val untouchedId = collection.listedSearchIndex("untouched")["id"]

            collection.recreateSearchIndex("replaced", ID_AND_TITLE_INDEX_DEFINITION)
            collection.waitForSearchIndexReady("replaced")

            collection.mappedFields("replaced") shouldBe setOf("_id", "title")
            collection.mappedFields("untouched") shouldBe setOf("_id")
            collection.listedSearchIndex("untouched")["id"] shouldBe untouchedId
        }

        "searchIndexExists should be true from the creation of an index until it is dropped" {
            val collectionName = uuid7()
            val collection = mongo.database.getCollection<Document>(collectionName)

            // Neither the collection nor the index exist yet
            collection.searchIndexExists(DEFAULT_MONGO_SEARCH_INDEX) shouldBe false
            mongo.database.createCollection(collectionName)
            collection.searchIndexExists(DEFAULT_MONGO_SEARCH_INDEX) shouldBe false

            // The index is listed from the moment it is created, whatever its status, and only under its own name
            collection.createDefaultSearchIndex(ID_INDEX_DEFINITION)
            collection.searchIndexExists(DEFAULT_MONGO_SEARCH_INDEX) shouldBe true
            collection.searchIndexExists("another-index") shouldBe false
            collection.waitForDefaultSearchIndexReady()
            collection.searchIndexExists(DEFAULT_MONGO_SEARCH_INDEX) shouldBe true

            collection.dropSearchIndex(DEFAULT_MONGO_SEARCH_INDEX)
            await("Waiting for the search index to be dropped")
                .atMost(Duration.ofSeconds(60))
                .until { !collection.searchIndexExists(DEFAULT_MONGO_SEARCH_INDEX) }
        }
    }
}

/** An index definition that maps only the `_id` field, which the tests create their first index from. */
private val ID_INDEX_DEFINITION = """
    {
        "mappings": {
            "dynamic": false,
            "fields": {
                "_id": {
                    "type": "string",
                    "analyzer": "lucene.keyword"
                }
            }
        }
    }
""".trimIndent()

/** [ID_INDEX_DEFINITION] with a `title` field added, which the tests replace it with. */
private val ID_AND_TITLE_INDEX_DEFINITION = """
    {
        "mappings": {
            "dynamic": false,
            "fields": {
                "_id": {
                    "type": "string",
                    "analyzer": "lucene.keyword"
                },
                "title": {
                    "type": "string",
                    "analyzer": "lucene.standard"
                }
            }
        }
    }
""".trimIndent()

/** The `listSearchIndexes` entry of the index named [indexName]; fails unless the index is listed exactly once. */
private fun MongoCollection<*>.listedSearchIndex(indexName: String): Document =
    listSearchIndexes().toList().single { it["name"] == indexName }

/** The names of the fields that the index named [indexName] maps, read from the `latestDefinition` Atlas reports for it. */
private fun MongoCollection<*>.mappedFields(indexName: String): Set<String> =
    listedSearchIndex(indexName).getEmbedded(listOf("latestDefinition", "mappings", "fields"), Document::class.java).keys.toSet()
