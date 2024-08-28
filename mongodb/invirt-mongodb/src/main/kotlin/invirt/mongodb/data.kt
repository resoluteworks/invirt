package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.geojson.Polygon
import com.mongodb.client.model.geojson.Position
import com.mongodb.kotlin.client.FindIterable
import invirt.data.DataFilter
import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
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
fun List<Sort>.mongoSort(): Bson? = if (this.isNotEmpty()) {
    Sorts.orderBy(map { it.mongoSort() })
} else {
    null
}

/**
 * Applies the specified [sort] to the [FindIterable].
 */
fun <T : Any> FindIterable<T>.sort(vararg sort: Sort = emptyArray()): FindIterable<T> = if (sort.isNotEmpty()) {
    this.sort(sort.toList().mongoSort())
} else {
    this
}

/**
 * Converts the specified [DataFilter] to a MongoDB [Bson] filter.
 * It handles both [DataFilter.Field] and [DataFilter.Compound] filters.
 */
fun DataFilter.mongoFilter(): Bson = when (this) {
    is DataFilter.Field<*> -> this.fieldFilter()
    is DataFilter.Compound -> {
        when (this.operator) {
            DataFilter.Compound.Operator.OR -> Filters.or(this.subFilters.map { it.mongoFilter() })
            DataFilter.Compound.Operator.AND -> Filters.and(this.subFilters.map { it.mongoFilter() })
        }
    }

    else -> throw IllegalArgumentException("Unknown filter type ${this::class}")
}

private fun GeoLocation.toPosition(): Position = Position(lng, lat)

private fun DataFilter.Field<*>.fieldFilter(): Bson = when (operation) {
    DataFilter.Field.Operation.EQ -> Filters.eq(field, value)
    DataFilter.Field.Operation.GT -> Filters.gt(field, value)
    DataFilter.Field.Operation.GTE -> Filters.gte(field, value)
    DataFilter.Field.Operation.LTE -> Filters.lte(field, value)
    DataFilter.Field.Operation.LT -> Filters.lt(field, value)
    DataFilter.Field.Operation.NE -> Filters.ne(field, value)
    DataFilter.Field.Operation.EXISTS -> Filters.exists(field)
    DataFilter.Field.Operation.DOESNT_EXIST -> Filters.exists(field, false)
    DataFilter.Field.Operation.WITHIN_GEO_BOUNDS -> {
        val geoBounds = value as GeoBoundingBox
        val positions = geoBounds.points.plus(geoBounds.southWest).map { it.toPosition() }
        Filters.geoWithin(field, Polygon(positions))
    }
}
