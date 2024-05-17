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

fun Request.filterCriteria(
    filterOptions: List<QueryFilterOption<*>>,
    operator: CompoundCriteria.Operator = CompoundCriteria.Operator.AND
): FilterCriteria? {
    val filtersCriteria = filterOptions
        .mapNotNull { option ->
            val optionCriteria = queries(option.field).mapNotNull { operationAndValue ->
                operationAndValue?.let {
                    FieldCriteria.of(option.field, operationAndValue).map { valueStr -> option.fromQueryValue(valueStr) }
                }
            }
            if (optionCriteria.isEmpty()) {
                null
            } else if (optionCriteria.size == 1) {
                optionCriteria.first()
            } else {
                CompoundCriteria(option.operator, optionCriteria)
            }
        }
    if (filtersCriteria.isEmpty()) {
        return null
    }
    if (filtersCriteria.size == 1) {
        return filtersCriteria.first()
    }
    return CompoundCriteria(operator, filtersCriteria)
}
