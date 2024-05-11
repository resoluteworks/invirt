package invirt.mongodb

import com.mongodb.client.model.CountOptions
import com.mongodb.kotlin.client.FindIterable
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import invirt.data.Sort
import org.bson.BsonDocument
import org.bson.conversions.Bson

interface MongoQuery {
    val page: Page
    val sort: List<Sort>
    val filter: Bson?
    val maxDocuments: Int get() = 1000
}

fun <E : StoredEntity> MongoCollection<E>.query(searchQuery: MongoQuery): RecordsPage<E> {
    return this.pagedQuery(searchQuery.filter, searchQuery.page, searchQuery.maxDocuments, *searchQuery.sort.toTypedArray())
}

fun <E : StoredEntity> MongoCollection<E>.pagedQuery(
    filter: Bson? = null,
    page: Page = Page(0, 10),
    maxDocuments: Int = 1000,
    vararg sorts: Sort = emptyArray()
): RecordsPage<E> {
    val countOptions = CountOptions().limit(maxDocuments)

    // This isn't optimal as it performs two Mongo queries, but the alternative is using
    // aggregations (see commented code below) and things get very complex, particularly with text search
    val (count: Long, iterable: FindIterable<E>) =
        if (filter != null) {
            Pair(this.countDocuments(filter, countOptions), this.find(filter).page(page))
        } else {
            Pair(this.countDocuments(BsonDocument(), countOptions), this.find().page(page))
        }

    return RecordsPage(
        records = iterable.sort(sorts.mongoSort()).toList(),
        count = count,
        page = page,
        sort = sorts.toList()
    )
}
