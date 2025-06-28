package invirt.core.data

import invirt.data.DataFilter
import invirt.data.andFilter
import invirt.data.orFilter
import org.http4k.core.Request
import org.http4k.lens.BiDiLens

/**
 * A filter that can be applied to a [Request] to extract values from query parameters and build a [DataFilter].
 * The filter can be used to build complex filters by combining multiple query parameters with different operators.
 * @param operator the operator to use when combining multiple filters.
 */
class QueryDataFilter(private val operator: DataFilter.Compound.Operator) {

    private val paramFilters = mutableListOf<QueryParamFilter<*>>()

    operator fun invoke(request: Request): DataFilter? {
        val filters = paramFilters.mapNotNull { it.getFilter(request) }
        if (filters.isEmpty()) {
            return null
        }
        return DataFilter.Compound(operator, filters)
    }

    infix fun <Value : Any> BiDiLens<Request, Value?>.filter(filter: (Value) -> DataFilter?) {
        paramFilters.add(QueryParamFilter(this, null, filter))
    }

    fun <Value : Any> BiDiLens<Request, Value?>.whenMissing(missing: () -> DataFilter) {
        paramFilters.add(QueryParamFilter(this, missing, null))
    }

    infix fun <Value : Any> BiDiLens<Request, List<Value>?>.or(filter: (Value) -> DataFilter) {
        paramFilters.add(
            QueryParamFilter(this) { values ->
                values.map { value -> filter(value) }.orFilter()
            }
        )
    }

    infix fun <Value : Any> BiDiLens<Request, List<Value>?>.and(filter: (Value) -> DataFilter) {
        paramFilters.add(
            QueryParamFilter(this) { values ->
                values.map { value -> filter(value) }.andFilter()
            }
        )
    }

    private class QueryParamFilter<Value>(
        val lens: BiDiLens<Request, Value?>,
        val missing: (() -> DataFilter)? = null,
        val filter: ((Value) -> DataFilter?)? = null
    ) {
        init {
            if (missing == null && filter == null) {
                throw IllegalArgumentException("Either missing filter or value filter required")
            }
        }

        fun getFilter(request: Request): DataFilter? {
            val value = lens(request)
            // When there's a missing value filter and params have been provided return null
            return if (missing != null) {
                if (value == null) {
                    missing.invoke()
                } else {
                    null
                }
            } else {
                value?.let { filter?.invoke(it) }
            }
        }
    }
}

/**
 * Creates a new [QueryDataFilter] with the given [operator] and applies the [build] function to it.
 *
 * @param operator the operator to use when combining multiple filters. Default is [DataFilter.Compound.Operator.AND].
 * @param build a lambda function to build the filter, where you can define the query parameters and their filters.
 * @return a [QueryDataFilter] instance that can be used to filter requests based on query parameters.
 */
fun queryDataFilter(
    operator: DataFilter.Compound.Operator = DataFilter.Compound.Operator.AND,
    build: QueryDataFilter.() -> Unit
): QueryDataFilter {
    val filter = QueryDataFilter(operator)
    build(filter)
    return filter
}
