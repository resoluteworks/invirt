package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.Page
import invirt.data.RecordsPage
import invirt.data.Sort
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

/**
 * A query builder for [MongoCollection] queries. Useful mainly for fluent operations on a collection.
 */
class MongoQuery<Doc : Any>(private val collection: MongoCollection<Doc>) {

    private var filter: Bson = Filters.empty()
    private var page: Page = Page(0, 10)
    private var sort: List<Bson> = emptyList()
    private var collation: Collation? = null

    /**
     * Sets the filter for the query.
     */
    fun filter(filter: Bson): MongoQuery<Doc> {
        this.filter = filter
        return this
    }

    /**
     * Sets the filter for the query to the logical AND of the given [filters].
     */
    fun andFilter(vararg filters: Bson): MongoQuery<Doc> = filter(Filters.and(filters.toList()))

    /**
     * Sets the filter for the query to the logical OR of the given [filters].
     */
    fun orFilter(vararg filters: Bson): MongoQuery<Doc> = filter(Filters.or(filters.toList()))

    /**
     * Sets the page for the query.
     */
    fun page(page: Page): MongoQuery<Doc> {
        this.page = page
        return this
    }

    /**
     * Sets the page for the query.
     */
    fun page(from: Int, size: Int): MongoQuery<Doc> = page(Page(from, size))

    /**
     * Adds a sort to the query.
     */
    fun sort(sort: Bson): MongoQuery<Doc> {
        this.sort = this.sort.plus(sort)
        return this
    }

    /**
     * Adds a sort to the query via an Invirt [Sort]
     */
    fun sort(sort: Sort): MongoQuery<Doc> = sort(sort.mongoSort())

    fun sort(sort: Collection<Bson>): MongoQuery<Doc> {
        this.sort = this.sort.plus(sort)
        return this
    }

    fun sort(sort: List<Sort>): MongoQuery<Doc> = sort(sort.map { it.mongoSort() })

    fun sortAsc(field: String): MongoQuery<Doc> = sort(Sorts.ascending(field))
    fun sortDesc(field: String): MongoQuery<Doc> = sort(Sorts.descending(field))
    fun sortAsc(property: KProperty<*>) = sortAsc(property.name)
    fun sortDesc(property: KProperty<*>) = sortDesc(property.name)

    /**
     * Sets the collation for the query.
     */
    fun collation(collation: Collation): MongoQuery<Doc> {
        this.collation = collation
        return this
    }

    /**
     * Executes the query and returns the result.
     */
    fun find(): RecordsPage<Doc> = collection.pagedQuery(
        filter = filter,
        page = page,
        sort = sort,
        buildFind = {
            collation?.let { this.collation(it) } ?: this
        }
    )
}

fun <Doc : Any> MongoCollection<Doc>.query(): MongoQuery<Doc> = MongoQuery(this)
