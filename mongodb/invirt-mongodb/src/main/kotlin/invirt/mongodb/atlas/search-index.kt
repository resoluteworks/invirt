package invirt.mongodb.atlas

import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.awaitility.Awaitility.await
import org.bson.Document
import java.time.Duration

private val log = KotlinLogging.logger {}

/**
 * Creates a search index with the specified [indexName] from the [definition] represented as a JSON string.
 */
fun MongoCollection<*>.createSearchIndex(indexName: String, definition: String) {
    createSearchIndex(indexName, Document.parse(definition))
    log.info { "Created Mongo search index $indexName" }
}

/**
 * Creates the default search index from the [definition] represented as a JSON string.
 * Atlas Search uses the `default` name when no index name is provided: https://www.mongodb.com/docs/atlas/atlas-search/create-index/
 */
fun MongoCollection<*>.createDefaultSearchIndex(definition: String) {
    createSearchIndex(Document.parse(definition))
    log.info { "Created default Mongo search index" }
}

fun MongoCollection<*>.waitForSearchIndexReady(indexName: String, seconds: Int = 60) {
    await("Mongo search index '${indexName}' ready")
        .atMost(Duration.ofSeconds(seconds.toLong()))
        .until {
            listSearchIndexes().toList()
                .firstOrNull { it["name"] == indexName }
                ?.let { it["status"] == "READY" }
                ?: false
        }
}

fun MongoCollection<*>.waitForDefaultSearchIndexReady(seconds: Int = 60) {
    await("Mongo default search index ready")
        .atMost(Duration.ofSeconds(seconds.toLong()))
        .until {
            listSearchIndexes().toList()
                .firstOrNull { it["name"] == "default" }
                ?.let { it["status"] == "READY" }
                ?: false
        }
}
