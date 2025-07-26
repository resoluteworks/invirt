package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.FindIterable
import invirt.data.DataFilter
import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import org.bson.conversions.Bson

/**
 * Applies the specified [page] to the [FindIterable] by skipping the first [Page.from] documents
 * and limiting the result to [Page.size] documents.
 */
fun <T : Any> FindIterable<T>.page(page: Page): FindIterable<T> = this.skip(page.from)
    .limit(page.size)

/**
 * Converts the specified [Sort] to a MongoDB [Bson] sort.
 */
fun Sort.mongoSort(): Bson = when (order) {
    SortOrder.ASC -> Sorts.ascending(field)
    SortOrder.DESC -> Sorts.descending(field)
}

/**
 * Converts the specified list of [Sort] to a MongoDB [Bson] sort.
 */
fun List<Sort>.mongoSort(): List<Bson> = this.map { it.mongoSort() }

/**
 * Applies the specified [sort] to the [FindIterable].
 */
fun <T : Any> FindIterable<T>.sort(sort: List<Sort>): FindIterable<T> = if (sort.isNotEmpty()) {
    this.sort(Sorts.orderBy(sort.mongoSort()))
} else {
    this
}

/**
 * Applies the specified [sort] to the [FindIterable].
 */
fun <T : Any> FindIterable<T>.sort(vararg sort: Sort = emptyArray()): FindIterable<T> = this.sort(sort.toList())

/**
 * Converts the specified [DataFilter] to a MongoDB [Bson] filter.
 * It handles [DataFilter.Field], [DataFilter.Or] and [DataFilter.And] filters.
 */
fun DataFilter.mongoFilter(): Bson = when (this) {
    is DataFilter.Field -> this.fieldFilter()
    is DataFilter.Or -> Filters.or(this.filters.map { it.mongoFilter() })
    is DataFilter.And -> Filters.and(this.filters.map { it.mongoFilter() })
}

private fun DataFilter.Field.fieldFilter(): Bson = when (this) {
    is DataFilter.Field.Eq<*> -> Filters.eq(field, value)
    is DataFilter.Field.Ne<*> -> Filters.ne(field, value)
    is DataFilter.Field.Gt<*> -> Filters.gt(field, value)
    is DataFilter.Field.Gte<*> -> Filters.gte(field, value)
    is DataFilter.Field.Lt<*> -> Filters.lt(field, value)
    is DataFilter.Field.Lte<*> -> Filters.lte(field, value)
    is DataFilter.Field.WithinGeoBounds -> field.mongoGeoBounds(value)
    is DataFilter.Field.Exists -> Filters.exists(field)
    is DataFilter.Field.DoesntExist -> Filters.exists(field, false)
}
