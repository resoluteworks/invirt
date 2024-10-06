package invirt.mongodb.atlas

import com.mongodb.client.model.search.SearchOperator
import invirt.mongo.test.testMongoAtlas
import invirt.mongo.test.waitForSearchDocuments
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.VersionedDocument
import invirt.mongodb.insert
import invirt.mongodb.mongoNow
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
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
    }
}
