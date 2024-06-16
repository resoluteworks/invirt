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
        paramFilters.add(QueryParamFilter(this, null, filter))
    }

    fun <Value : Any> BiDiLens<Request, Value?>.whenMissing(missing: Filter? = null) {
        paramFilters.add(QueryParamFilter(this, missing, null))
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
        val missing: Filter? = null,
        val filter: ((Value) -> Filter?)? = null
    ) {
        init {
            if (missing == null && filter == null) {
                throw IllegalArgumentException("Either missing filter or value filter required")
            }
        }

        fun getFilter(request: Request): Filter? {
            val values = lens(request)
            // When there's a missing value filter and params have been provided return null
            return if (missing != null) {
                if (values == null) {
                    missing
                } else {
                    null
                }
            } else {
                values?.let { filter?.invoke(it) }
            }
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
