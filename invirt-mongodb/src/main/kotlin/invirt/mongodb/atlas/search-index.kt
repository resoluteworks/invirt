package invirt.mongodb.atlas

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.search.SearchOperator
import com.mongodb.client.model.search.SearchOptions
import com.mongodb.client.model.search.SearchPath
import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.awaitility.Awaitility.await
import org.bson.Document
import java.time.Duration

private val log = KotlinLogging.logger {}

/**
 * Creates the default search index from the [definition] represented as a JSON string.
 * Atlas Search uses the `default` name when no index name is provided: https://www.mongodb.com/docs/atlas/atlas-search/create-index/
 */
fun MongoCollection<*>.createDefaultSearchIndex(definition: String) {
    createSearchIndex(Document.parse(definition))
    log.info { "Created default Mongo search index" }
}

fun MongoCollection<*>.recreateDefaultSearchIndex(definition: String) {
    recreateSearchIndex(DEFAULT_MONGO_SEARCH_INDEX, definition)
}

/**
 * Recreates the search index [indexName] from the [definition] represented as a JSON string.
 */
fun MongoCollection<*>.recreateSearchIndex(indexName: String, definition: String) {
    if (searchIndexReady(indexName)) {
        dropSearchIndex(indexName)
    }
    await("Waiting for search index '${indexName}' to be removed")
        .atMost(Duration.ofSeconds(60))
        .until { !searchIndexReady(indexName) }

    createSearchIndex(indexName, Document.parse(definition))
    log.info { "Created default Mongo search index" }
}

/**
 * Waits for the search index with the given [indexName] to be ready.
 */
fun MongoCollection<*>.waitForSearchIndexReady(indexName: String, seconds: Int = 60) {
    await("Mongo search index '${indexName}' ready")
        .atMost(Duration.ofSeconds(seconds.toLong()))
        .until { searchIndexReady(indexName) }
}

/**
 * Waits for the default search index to be ready.
 */
fun MongoCollection<*>.waitForDefaultSearchIndexReady(seconds: Int = 60) {
    await("Mongo default search index ready")
        .atMost(Duration.ofSeconds(seconds.toLong()))
        .until { searchIndexReady(DEFAULT_MONGO_SEARCH_INDEX) }
}

fun MongoCollection<*>.searchIndexReady(indexName: String): Boolean =
    listSearchIndexes().toList().any { it["name"] == indexName && it["status"] == "READY" }

/**
 * Waits for the default search index to contain a document with the given [id].
 * This assumes that the index configuration has a mapping like this:
 *
 * ```
 *     "_id": {
 *         "type": "string",
 *         "analyzer": "lucene.keyword"
 *     }
 * ```
 *
 * @param id The id of the document to search.
 * @param seconds The maximum time to wait. Default is 60 seconds.
 */
fun MongoCollection<*>.waitForDocumentWithIdInDefaultSearchIndex(
    id: String,
    seconds: Int = 60
) {
    val searchOperator = SearchOperator.text(SearchPath.fieldPath("_id"), id)
    waitForDocumentsInDefaultSearchIndex(searchOperator, 1, seconds)
}

/**
 * Waits for the default search index to contain a document with the given [fieldName] and [fieldValue].
 * @param fieldName The name of the field to search.
 * @param fieldValue The value of the field to search.
 * @param seconds The maximum time to wait. Default is 60 seconds.
 */
fun MongoCollection<*>.waitForDocumentInDefaultSearchIndex(
    fieldName: String,
    fieldValue: String,
    seconds: Int = 60
) {
    val searchOperator = SearchOperator.text(SearchPath.fieldPath(fieldName), fieldValue)
    waitForDocumentsInDefaultSearchIndex(searchOperator, 1, seconds)
}

/**
 * Waits for the default search index to contain [documentCount] documents.
 * @param searchOperator The search operator to use.
 * @param documentCount The number of documents to wait for.
 * @param seconds The maximum time to wait. Default is 60 seconds.
 */
fun MongoCollection<*>.waitForDocumentsInDefaultSearchIndex(
    searchOperator: SearchOperator,
    documentCount: Int,
    seconds: Int = 60
) {
    waitForDocumentsInSearchIndex(DEFAULT_MONGO_SEARCH_INDEX, searchOperator, documentCount, seconds)
}

/**
 * Waits for the search index with the given [indexName] to contain [documentCount] documents.
 * @param indexName The name of the search index.
 * @param searchOperator The search operator to use.
 * @param documentCount The number of documents to wait for.
 * @param seconds The maximum time to wait. Default is 60 seconds.
 */
fun MongoCollection<*>.waitForDocumentsInSearchIndex(
    indexName: String,
    searchOperator: SearchOperator,
    documentCount: Int,
    seconds: Int = 60
) {
    val pipeline = mutableListOf(
        Aggregates.search(
            searchOperator,
            SearchOptions.searchOptions().index(indexName)
        )
    )
    await("Waiting for $documentCount to be indexed in search index '$indexName' for collection ${this.namespace.collectionName}")
        .atMost(Duration.ofSeconds(seconds.toLong()))
        .until {
            aggregate(pipeline).toList().size == documentCount
        }
}
