@file:Suppress("UNCHECKED_CAST")

package invirt.mongodb

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Facet
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

/**
 * Runs a [MongoCollection.aggregate] for the specified [pipeline] with the given [page]
 * and optional [facets]. Returns a [PagedAggregateSearchResult] containing
 * the [RecordsPage] for the specified page and the raw response document.
 *
 * [facets] and [documentStages] are both lists of stages, and they land in different places.
 * A [Facet] in [facets] is a sibling of the `documents` facet: it runs over the whole matched
 * population, which is what an aggregated count or a breakdown of the full result set needs.
 * [documentStages] run *inside* the `documents` facet, after its `$skip` and `$limit`, so they
 * see the page's rows only. That is the place for a per-row `$lookup` or `$addFields` which
 * would otherwise be paid for across the entire population.
 *
 * Because [documentStages] run after `$skip` / `$limit`, a `$match` or a `$sort` there disagrees
 * with `totalCount` and with the paging: the match drops rows the count still includes, and the
 * sort only orders the rows of the page it is handed. Narrowing and ordering belong in [pipeline].
 */
fun <Doc : Any> MongoCollection<Doc>.pagedAggregate(
    pipeline: List<Bson>,
    page: Page,
    facets: List<Facet> = emptyList(),
    documentStages: List<Bson> = emptyList()
): PagedAggregateSearchResult<Doc> {
    val allFacets = listOf(
        Facet("documents", listOf(Aggregates.skip(page.from), Aggregates.limit(page.size)).plus(documentStages)),
        Facet("totalCount", Aggregates.count())
    ).plus(facets)

    val results = withDocumentClass<Document>()
        .aggregate(pipeline.plus(Aggregates.facet(allFacets)))
        .toList()
        .first()

    val recordsPage = RecordsPage(
        records = (results["documents"] as List<Document>).mongoDeserializeWith(this.documentClass.kotlin),
        totalCount = results.countFacet("totalCount").toLong(),
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

/**
 * Reads the count out of the `$facet` branch [name], which is expected to end in a `$count` stage.
 *
 * A `$count` stage emits no document at all when nothing matched, so an empty branch - and a branch
 * that is not in the result at all - read as 0.
 */
fun Document.countFacet(name: String): Int = getList(name, Document::class.java)?.firstOrNull()?.getInteger("count") ?: 0

/**
 * Deserialises the documents of the `$facet` branch [name] into [Doc] using the default Mongo codec
 * registry. A branch that is empty, or not in the result at all, reads as an empty list.
 */
inline fun <reified Doc : Any> Document.facetListOf(name: String): List<Doc> =
    getList(name, Document::class.java)?.mongoDeserializeWith() ?: emptyList()

/**
 * Runs [pipeline] and returns the number of documents it leaves, appending the `$count` stage itself
 * so the caller never names the field the count lands under.
 *
 * A pipeline that matches nothing produces no document at all rather than a zero, which this reads
 * as 0.
 */
fun MongoCollection<*>.aggregateCount(pipeline: List<Bson>): Long = withDocumentClass<Document>()
    .aggregate(pipeline.plus(Aggregates.count()))
    .firstOrNull()
    ?.getInteger("count")
    ?.toLong()
    ?: 0L

/**
 * Counts the documents whose [field] is one of [ids], grouped by [field], as a single aggregation
 * rather than a count per id. [extraFilter] narrows the documents that are counted; it is combined
 * with the [field] match.
 *
 * An id with no documents is absent from the map rather than present with a zero, so a caller reads
 * it as `counts[id] ?: 0`. An empty [ids] returns an empty map without querying, which is
 * correctness rather than an optimisation: [mongoIn] rejects an empty collection.
 *
 * The map is typed to the pair this counts: a [String] group key, so [field] must hold strings, and
 * an [Int] count.
 */
fun <Doc : Any> MongoCollection<Doc>.countsGroupedBy(
    field: KProperty<*>,
    ids: Collection<String>,
    extraFilter: Bson? = null
): Map<String, Int> {
    if (ids.isEmpty()) {
        return emptyMap()
    }
    val pipeline = listOf(
        Aggregates.match(mongoAnd(field.mongoIn(ids), extraFilter)),
        Aggregates.group("\$${field.name}", Accumulators.sum("count", 1))
    )
    return withDocumentClass<Document>()
        .aggregate(pipeline)
        .toList()
        .associate { it.getString("_id") to it.getInteger("count") }
}
