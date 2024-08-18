package invirt.mongodb

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import org.bson.conversions.Bson

fun <Doc : Any> MongoCollection<Doc>.query(
    filter: Bson? = null,
    page: Page = Page(0, 10),
    maxDocuments: Int = 1000,
    sort: List<Bson> = emptyList()
): RecordsPage<Doc> {
    val countOptions = CountOptions().limit(maxDocuments)
    val docFilter = filter ?: Filters.empty()

    var find = this.find(docFilter)
    if (sort.isNotEmpty()) {
        find = find.sort(Sorts.orderBy(sort))
    }

    return RecordsPage(
        records = find.page(page).toList(),
        totalCount = this.countDocuments(docFilter, countOptions),
        page = page
    )
}
