package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.FindIterable
import invirt.data.*
import org.bson.conversions.Bson

fun <T : Any> FindIterable<T>.page(page: Page): FindIterable<T> {
    return this.skip(page.from)
        .limit(page.size)
}

fun Sort.mongoSort(): Bson {
    return when (order) {
        SortOrder.ASC -> Sorts.ascending(field)
        SortOrder.DESC -> Sorts.descending(field)
    }
}

fun List<Sort>.mongoSort(): Bson? {
    return if (this.isNotEmpty()) {
        Sorts.orderBy(map { it.mongoSort() })
    } else {
        null
    }
}

fun Array<out Sort>.mongoSort(): Bson? {
    return if (this.isNotEmpty()) {
        Sorts.orderBy(map { it.mongoSort() })
    } else {
        null
    }
}

fun <T : Any> FindIterable<T>.sort(vararg sort: Sort = emptyArray()): FindIterable<T> {
    return if (sort.isNotEmpty()) {
        this.sort(sort.toList().mongoSort())
    } else {
        this
    }
}

private fun <Value : Any> FieldFilter<Value>.mongoFilter(): Bson {
    return when (operation) {
        FieldFilter.Operation.EQ -> Filters.eq(field, value)
        FieldFilter.Operation.GT -> Filters.gt(field, value)
        FieldFilter.Operation.GTE -> Filters.gte(field, value)
        FieldFilter.Operation.LTE -> Filters.lte(field, value)
        FieldFilter.Operation.LT -> Filters.lt(field, value)
        FieldFilter.Operation.NE -> Filters.ne(field, value)
    }
}

fun Filter.mongoFilter(): Bson {
    return when (this) {
        is FieldFilter<*> -> this.mongoFilter()
        is CompoundFilter -> {
            when (this.operator) {
                CompoundFilter.Operator.OR -> Filters.or(this.children.map { it.mongoFilter() })
                CompoundFilter.Operator.AND -> Filters.and(this.children.map { it.mongoFilter() })
            }
        }

        else -> throw IllegalArgumentException("Unknown filter type ${this::class}")
    }
}
