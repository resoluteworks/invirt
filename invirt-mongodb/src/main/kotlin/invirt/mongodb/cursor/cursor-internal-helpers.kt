package invirt.mongodb.cursor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import invirt.data.Sort
import invirt.data.SortOrder
import invirt.mongodb.mongoSort
import org.bson.conversions.Bson

internal val jsonMapper = jacksonObjectMapper()

internal fun List<Sort>.forwardSort(): Bson = Sorts.orderBy(this.map { it.mongoSort() })

internal fun List<Sort>.reverseSort(): Bson = Sorts.orderBy(this.map { it.revert().mongoSort() })

internal fun Sort.eq(value: Any): Bson = Filters.eq(this.field, value)

internal fun Sort.gt(value: Any): Bson = when (order) {
    SortOrder.ASC -> Filters.gt(this.field, value)
    SortOrder.DESC -> Filters.lt(this.field, value)
}

internal fun Sort.lt(value: Any): Bson = when (order) {
    SortOrder.ASC -> Filters.lt(this.field, value)
    SortOrder.DESC -> Filters.gt(this.field, value)
}
