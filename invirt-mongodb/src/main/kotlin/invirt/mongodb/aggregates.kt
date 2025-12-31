@file:Suppress("UNCHECKED_CAST")

package invirt.mongodb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Facet
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import org.bson.Document
import org.bson.conversions.Bson

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
        records = (results["documents"] as List<Document>).mongoDeserializeWith(this.documentClass.kotlin),
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
