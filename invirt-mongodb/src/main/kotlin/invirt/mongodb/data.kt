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

private fun <Value : Any> FieldCriteria<Value>.mongoFilter(): Bson {
    return when (operation) {
        FieldCriteria.Operation.EQ -> Filters.eq(field, value)
        FieldCriteria.Operation.GT -> Filters.gt(field, value)
        FieldCriteria.Operation.GTE -> Filters.gte(field, value)
        FieldCriteria.Operation.LTE -> Filters.lte(field, value)
        FieldCriteria.Operation.LT -> Filters.lt(field, value)
        FieldCriteria.Operation.NE -> Filters.ne(field, value)
    }
}

fun FilterCriteria.mongoFilter(): Bson {
    return when (this) {
        is FieldCriteria<*> -> this.mongoFilter()
        is CompoundCriteria -> {
            when (this.operator) {
                CompoundCriteria.Operator.OR -> Filters.or(this.children.map { it.mongoFilter() })
                CompoundCriteria.Operator.AND -> Filters.and(this.children.map { it.mongoFilter() })
            }
        }

        else -> throw IllegalArgumentException("Unknown filter criteria ${this::class}")
    }
}
