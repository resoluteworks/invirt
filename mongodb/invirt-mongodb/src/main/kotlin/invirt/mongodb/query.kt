package invirt.mongodb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.Facet
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import org.bson.Document
import org.bson.conversions.Bson

/**
 * Runs a [MongoCollection.find] for the specified [filter] with the given [page] and [sort].
 * Optionally, a [maxDocuments] limit can be set to restrict the total number of documents to count.
 *
 * @return a [RecordsPage] with the documents matching the filter and page.
 */
fun <Doc : Any> MongoCollection<Doc>.query(
    filter: Bson? = null,
    page: Page = Page(0, 10),
    sort: List<Bson> = emptyList(),
    maxDocuments: Int = 0
): RecordsPage<Doc> {
    val docFilter = filter ?: Filters.empty()

    var find = this.find(docFilter)
    if (sort.isNotEmpty()) {
        find = find.sort(Sorts.orderBy(sort))
    }

    return RecordsPage(
        records = find.page(page).toList(),
        totalCount = this.countDocuments(docFilter, CountOptions().limit(maxDocuments)),
        page = page
    )
}

/**
 * Runs a [MongoCollection.aggregate] for the specified [pipeline] with the given [page]
 * and optional [facets]. Returns a [PagedAggregateSearchResult] containing
 * the [RecordsPage] for the specified page and the raw response document.
 */
fun <Doc : Any> MongoCollection<Doc>.pagedAggregate(
    pipeline: List<Bson>,
    page: Page,
    facets: List<Facet> = emptyList()
): PagedAggregateSearchResult<Doc> {
    val allFacets = listOf(
        Facet("documents", Aggregates.skip(page.from), Aggregates.limit(page.size)),
        Facet("totalCount", Aggregates.count())
    ).plus(facets)

    val results = withDocumentClass<Document>()
        .aggregate(pipeline.plus(Aggregates.facet(allFacets)))
        .toList()
        .first()

    val recordsPage = RecordsPage(
        records = deserialize(results["documents"] as List<Document>),
        totalCount = (results["totalCount"] as List<Document>).firstOrNull()?.getInteger("count")?.toLong() ?: 0L,
        page = page
    )

    return PagedAggregateSearchResult(recordsPage, results)
}

/**
 * Contains the result of a [MongoCollection.pagedAggregate] call as a [RecordsPage]
 * and a [rawResult] with the original MongoDB aggregate response.
 */
class PagedAggregateSearchResult<Doc : Any>(
    val recordsPage: RecordsPage<Doc>,
    val rawResult: Document
)
