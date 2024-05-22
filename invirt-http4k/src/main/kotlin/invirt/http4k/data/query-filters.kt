package invirt.http4k.data

import invirt.data.CompoundFilter
import invirt.data.Filter
import invirt.data.andFilter
import invirt.data.orFilter
import org.http4k.core.Request
import org.http4k.lens.BiDiLens

class QueryValuesFilter(private val operator: CompoundFilter.Operator) {

    private val paramFilters = mutableListOf<QueryParamFilter<*>>()

    operator fun invoke(request: Request): Filter? {
        val filters = paramFilters.mapNotNull { it.getFilter(request) }
        if (filters.isEmpty()) {
            return null
        }
        return CompoundFilter(operator, filters)
    }

    infix fun <Value : Any> BiDiLens<Request, Value?>.filter(filter: (Value) -> Filter?) {
        paramFilters.add(QueryParamFilter(this, filter))
    }

    infix fun <Value : Any> BiDiLens<Request, List<Value>?>.or(filter: (Value) -> Filter) {
        paramFilters.add(
            QueryParamFilter(this) { values ->
                values.map { value -> filter(value) }.orFilter()
            }
        )
    }

    infix fun <Value : Any> BiDiLens<Request, List<Value>?>.and(filter: (Value) -> Filter) {
        paramFilters.add(
            QueryParamFilter(this) { values ->
                values.map { value -> filter(value) }.andFilter()
            }
        )
    }

    private class QueryParamFilter<Value>(
        val lens: BiDiLens<Request, Value?>,
        val filter: (Value) -> Filter?
    ) {
        fun getFilter(request: Request): Filter? {
            return lens(request)?.let { filter(it) }
        }
    }
}

fun queryValuesFilter(
    operator: CompoundFilter.Operator = CompoundFilter.Operator.AND,
    build: QueryValuesFilter.() -> Unit
): QueryValuesFilter {
    val filter = QueryValuesFilter(operator)
    build(filter)
    return filter
}
