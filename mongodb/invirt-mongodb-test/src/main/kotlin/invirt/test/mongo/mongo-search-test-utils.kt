package invirt.test.mongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.search.SearchOperator
import com.mongodb.client.model.search.SearchOptions
import com.mongodb.client.model.search.SearchPath
import com.mongodb.kotlin.client.MongoCollection
import invirt.mongodb.atlas.DEFAULT_MONGO_SEARCH_INDEX
import org.awaitility.Awaitility.await
import java.time.Duration

/**
 * Waits for [count] documents to be indexed by checking the existed of a [field] in the specified [indexName].
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
