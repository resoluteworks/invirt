package invirt.mongodb

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.FindIterable
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import org.bson.BsonDocument
import org.bson.conversions.Bson

interface MongoQuery {
    val page: Page
    val sort: List<Bson>
    val filter: Bson?
    val maxDocuments: Int get() = 1000
}

fun <E : StoredEntity> MongoCollection<E>.query(searchQuery: MongoQuery): RecordsPage<E> =
    this.query(searchQuery.filter, searchQuery.page, searchQuery.maxDocuments, searchQuery.sort)

fun <E : StoredEntity> MongoCollection<E>.query(
    filter: Bson? = null,
    page: Page = Page(0, 10),
    maxDocuments: Int = 1000,
    sort: List<Bson> = emptyList()
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
        records = iterable.sort(Sorts.orderBy(sort)).toList(),
        totalCount = count,
        page = page
    )
}
