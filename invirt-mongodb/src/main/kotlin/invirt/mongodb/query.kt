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
    filter: Bson = Filters.empty(),
    page: Page = Page(0, 10),
    sort: List<Bson> = emptyList(),
    maxDocuments: Int = 0,
    buildFind: FindIterable<Doc>.() -> FindIterable<Doc> = { this }
): RecordsPage<Doc> {
    var find = this.find(filter)
    if (sort.isNotEmpty()) {
        find = find.sort(Sorts.orderBy(sort))
    }
    find = buildFind(find)

    return RecordsPage(
        records = find.page(page).toList(),
        totalCount = this.countDocuments(filter, CountOptions().limit(maxDocuments)),
        page = page
    )
}

/**
 * Convenience overload of [pagedQuery] for a single [sort] parameter.
 */
fun <Doc : Any> MongoCollection<Doc>.pagedQuery(
    filter: Bson = Filters.empty(),
    page: Page = Page(0, 10),
    sort: Bson,
    maxDocuments: Int = 0,
    buildFind: FindIterable<Doc>.() -> FindIterable<Doc> = { this }
): RecordsPage<Doc> = pagedQuery(
    filter = filter,
    page = page,
    sort = listOf(sort),
    maxDocuments = maxDocuments,
    buildFind = buildFind
)
