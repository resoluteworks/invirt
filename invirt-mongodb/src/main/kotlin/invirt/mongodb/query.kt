package invirt.mongodb

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.FindIterable
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import org.bson.conversions.Bson

/**
 * Runs a [MongoCollection.find] for the specified [filter] with the given [page] and [sort].
 * Optionally, a [maxDocuments] limit can be set to restrict the total number of documents to count.
 *
 * @return a [RecordsPage] with the documents matching the filter and page.
 */
fun <Doc : Any> MongoCollection<Doc>.pagedQuery(
    filter: Bson? = null,
    page: Page = Page(0, 10),
    sort: List<Bson> = emptyList(),
    maxDocuments: Int = 0,
    buildFind: FindIterable<Doc>.() -> FindIterable<Doc> = { this }
): RecordsPage<Doc> {
    val docFilter = filter ?: Filters.empty()

    var find = this.find(docFilter)
    if (sort.isNotEmpty()) {
        find = find.sort(Sorts.orderBy(sort))
    }
    find = buildFind(find)

    return RecordsPage(
        records = find.page(page).toList(),
        totalCount = this.countDocuments(docFilter, CountOptions().limit(maxDocuments)),
        page = page
    )
}

fun <Doc : Any> MongoCollection<Doc>.query(): MongoQuery<Doc> = MongoQuery(this)
