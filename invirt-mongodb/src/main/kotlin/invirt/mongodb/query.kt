package invirt.mongodb

import com.mongodb.client.model.Aggregates.*
import com.mongodb.client.model.Facet
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import invirt.data.Sort
import org.bson.Document
import org.bson.codecs.DecoderContext
import org.bson.conversions.Bson
import org.bson.json.JsonReader
import kotlin.reflect.KClass

interface MongoQuery {
    val page: Page
    val sort: List<Sort>
    val filter: Bson?
    val maxDocuments: Int get() = 1000
}

inline fun <reified E : StoredEntity> MongoCollection<E>.query(searchQuery: MongoQuery): RecordsPage<E> = this.pagedQuery(
    entityClass = E::class,
    filter = searchQuery.filter,
    page = searchQuery.page,
    maxDocuments = searchQuery.maxDocuments,
    sorts = searchQuery.sort.toTypedArray()
)

fun <E : StoredEntity> MongoCollection<E>.pagedQuery(
    entityClass: KClass<E>,
    filter: Bson? = null,
    page: Page = Page(0, 10),
    maxDocuments: Int = 1000,
    vararg sorts: Sort = emptyArray()
): RecordsPage<E> {
    val queryPipeline: MutableList<Bson> = mutableListOf(
        match(filter ?: Filters.empty())
    )
    sorts.mongoSort()?.let { queryPipeline.add(sort(it)) }
    queryPipeline.add(skip(page.from))
    queryPipeline.add(limit(page.size))

    val countPipeline = listOf(match(filter ?: Filters.empty()), count())
    val result = this.withDocumentClass<QueryResult>().aggregate(
        listOf(
            facet(
                Facet("results", queryPipeline),
                Facet("count", countPipeline)
            )
        )
    ).first()

    val records = result.results.map {
        this.codecRegistry.get(entityClass.java).decode(JsonReader(it.toJson()), DecoderContext.builder().build())
    }
    return RecordsPage(
        records = records,
        count = if (result.count.isNotEmpty()) result.count.first().count else 0,
        page = page,
        sort = sorts.toList()
    )
}

data class QueryResult(val results: List<Document>, val count: List<CountDto>) {
    data class CountDto(val count: Long)
}
