package invirt.http4k.data

import invirt.data.*
import org.http4k.core.Request
import org.http4k.lens.Query
import org.http4k.lens.int

private val fromQuery = Query.int().optional("from")
private val sizeQuery = Query.int().optional("size")
private val sortQuery = Query.optional("sort")

fun Request.page(defaultFrom: Int = 0, defaultSize: Int = 10, maxSize: Int = defaultSize): Page {
    val from = fromQuery(this)
    val size = sizeQuery(this)
    return Page(
        from = from ?: defaultFrom,
        size = (size ?: defaultSize).coerceAtMost(maxSize)
    )
}

fun Request.sort(): Sort? {
    val sortStr = sortQuery(this) ?: return null
    val elements = sortStr.split(":")
    if (elements.size != 2) {
        return Sort(elements.first(), SortOrder.ASC)
    }
    return Sort(elements[0], SortOrder.valueOf(elements[1].uppercase()))
}

fun Request.queryFieldFilters(filterOptions: List<QueryFilterOption<*>>): List<FieldFilter<*>> {
    return filterOptions.flatMap { option ->
        queries(option.field).mapNotNull { operationAndValue ->
            operationAndValue?.let {
                FieldFilter.of(option.field, operationAndValue).map { valueStr -> option.fromQueryValue(valueStr) }
            }
        }
    }
}

fun Request.queryParamsAsFilter(
    filterOptions: List<QueryFilterOption<*>>,
    operator: CompoundFilter.Operator = CompoundFilter.Operator.AND
): Filter? {
    val filters = (filterOptions as List<QueryFilterOption<Any>>).mapNotNull { option ->
        val optionFilters = queries(option.field).mapNotNull { operationAndValue ->
            operationAndValue?.let {
                FieldFilter.of(option.field, operationAndValue).map { valueStr -> option.fromQueryValue(valueStr) }
            }
        }

        option.getFilter(optionFilters)
    }
    if (filters.isEmpty()) {
        return null
    }
    if (filters.size == 1) {
        return filters.first()
    }
    return CompoundFilter(operator, filters)
}
