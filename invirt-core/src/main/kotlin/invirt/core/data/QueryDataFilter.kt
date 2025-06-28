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
class QueryDataFilter(private val operator: Operator) {

    enum class Operator { AND, OR }

    private val paramFilters = mutableListOf<QueryParamFilter<*>>()

    /**
     * Applies the filter to a [Request] and returns a [DataFilter] if any of the query parameters match.
     * If no parameters match, it returns null.
     *
     * @param request the [Request] to filter.
     * @return a [DataFilter] or null if no filters were applied.
     */
    operator fun invoke(request: Request): DataFilter? {
        val filters = paramFilters.mapNotNull { it.getFilter(request) }
        if (filters.isEmpty()) {
            return null
        }
        return when (operator) {
            Operator.AND -> andFilter(filters)
            Operator.OR -> orFilter(filters)
        }
    }

    /**
     * Adds a query parameter filter to the [QueryDataFilter].
     *
     * @param [this] the [BiDiLens] used to extract the value from the request.
     * @param filter the function that takes the value and returns a [DataFilter] or null.
     */
    infix fun <Value : Any> BiDiLens<Request, Value?>.filter(filter: (Value) -> DataFilter?) {
        paramFilters.add(QueryParamFilter(this, null, filter))
    }

    /**
     * Adds a query parameter filter that applies when the value is missing.
     *
     * @param [this] the [BiDiLens] used to extract the value from the request.
     * @param missing the function that returns a [DataFilter] when the value is missing.
     */
    fun <Value : Any> BiDiLens<Request, Value?>.whenMissing(missing: () -> DataFilter) {
        paramFilters.add(QueryParamFilter(this, missing, null))
    }

    /**
     * Adds a query parameter filter that created an "or" [DataFilter] from the values extracted from the request.
     *
     * @param [this] the [BiDiLens] used to extract the value from the request.
     * @param filter the function that takes the value and returns a [DataFilter].
     */
    infix fun <Value : Any> BiDiLens<Request, List<Value>?>.or(filter: (Value) -> DataFilter) {
        paramFilters.add(
            QueryParamFilter(this) { values ->
                orFilter(values.map { value -> filter(value) })
            }
        )
    }

    infix fun <Value : Any> BiDiLens<Request, List<Value>?>.and(filter: (Value) -> DataFilter) {
        paramFilters.add(
            QueryParamFilter(this) { values ->
                andFilter(values.map { value -> filter(value) })
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
 * @param operator the operator to use when combining multiple filters. Default is [QueryDataFilter.Operator.AND].
 * @param build a lambda function to build the filter, where you can define the query parameters and their filters.
 * @return a [QueryDataFilter] instance that can be used to filter requests based on query parameters.
 */
fun queryDataFilter(
    operator: QueryDataFilter.Operator = QueryDataFilter.Operator.AND,
    build: QueryDataFilter.() -> Unit
): QueryDataFilter {
    val filter = QueryDataFilter(operator)
    build(filter)
    return filter
}
