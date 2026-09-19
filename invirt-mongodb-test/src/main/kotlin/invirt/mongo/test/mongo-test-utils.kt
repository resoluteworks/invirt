package invirt.mongo.test

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.search.SearchOperator
import com.mongodb.client.model.search.SearchOptions
import com.mongodb.client.model.search.SearchPath
import com.mongodb.kotlin.client.MongoCollection
import invirt.mongodb.Mongo
import invirt.mongodb.atlas.DEFAULT_MONGO_SEARCH_INDEX
import invirt.utils.uuid7
import org.awaitility.Awaitility.await
import org.bson.Document
import org.bson.conversions.Bson
import java.time.Duration

inline fun <reified Doc : Any> Mongo.randomTestCollection(): MongoCollection<Doc> = database.getCollection<Doc>(uuid7())

/**
 * Waits for [count] documents to be indexed by checking the existence of a [field] in the specified [indexName].
 */
fun MongoCollection<*>.waitForSearchDocuments(
    field: String,
    count: Int,
    indexName: String = DEFAULT_MONGO_SEARCH_INDEX
) {
    val pipeline = listOf(
        Aggregates.search(
            SearchOperator.exists(SearchPath.fieldPath(field)),
            SearchOptions.searchOptions().index(indexName)
        )
    )
    await("Mongo search index '$indexName' contains $count documents")
        .atMost(Duration.ofSeconds(30))
        .until { aggregate(pipeline).toList().size == count }
}

/**
 * Deletes every document from every collection in the database, except those matching [keep] and any
 * collection whose name contains one of [skipCollectionNamesContaining] - by default the mongock
 * bookkeeping collections (`mongockLock`, `mongockChangeLog`). Deletes documents rather than dropping
 * collections, so indices a migration created survive the truncation.
 *
 * A null [keep] deletes every document: there is then no filter to negate, so it resolves to
 * [Filters.empty], not to keeping everything.
 */
fun Mongo.clearCollections(keep: Bson? = null, skipCollectionNamesContaining: List<String> = listOf("mongock")) {
    database.listCollectionNames().toList()
        .filter { name -> skipCollectionNamesContaining.none { name.contains(it) } }
        .forEach { name ->
            database.getCollection<Document>(name).deleteMany(keep?.let { Filters.not(it) } ?: Filters.empty())
        }
}
