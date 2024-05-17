package invirt.http4k.data

import invirt.data.CompoundFilter
import invirt.data.FieldFilter
import invirt.data.Filter
import org.http4k.core.Request

@Suppress("UNCHECKED_CAST")
class RequestQueryFilters(
    val options: List<QueryFilterOption<*>>,
    val selected: List<FieldFilter<*>>,
    val filter: Filter?
) {

    private val selectedValues: Map<String, Set<Any>> = selected
        .groupBy { it.field }
        .map { group -> group.key to group.value.map { it.value }.toSet() }
        .toMap()

    fun selected(field: String, value: Any): Boolean {
        return selectedValues[field]?.contains(value) ?: false
    }

    constructor(request: Request, options: List<QueryFilterOption<*>>) : this(
        options = options,
        selected = request.queryFieldFilters(options),
        filter = request.queryParamsAsFilter(options as List<QueryFilterOption<Any>>)
    )
}

class RequestFilter(
    val fields: String,
    val values: (Request) -> Map<String, Set<String>>
)

// interface RequestFilter {
//
//    val fields: Set<String>
//    fun getSelectedValues(request: Request): Filter?
//
//    fun invoke(request: Request): Filter? {
//        val values: Map<String, Set<String>> = fields.associateWith { field ->
//            request.queries(field).filterNotNull().toSet()
//        }
//        return getFilter(values)
//    }
// }

class QueryFilterOption<Value : Any>(
    val field: String,
    val values: List<Value>,
    val toQueryValue: (Value) -> String,
    val fromQueryValue: (String) -> Value,
    val getFilter: (Collection<FieldFilter<Value>>) -> Filter?
) {

    companion object {
        val OR: (Collection<FieldFilter<*>>) -> Filter? = {
            when {
                it.isEmpty() -> null
                it.size == 1 -> it.first()
                else -> CompoundFilter(CompoundFilter.Operator.OR, it)
            }
        }

        val AND: (Collection<FieldFilter<*>>) -> Filter? = {
            when {
                it.isEmpty() -> null
                it.size == 1 -> it.first()
                else -> CompoundFilter(CompoundFilter.Operator.AND, it)
            }
        }
    }
}

fun stringFilterOption(
    field: String,
    values: List<String>,
    getFilter: (Collection<FieldFilter<String>>) -> Filter? = QueryFilterOption.OR
): QueryFilterOption<String> {
    return QueryFilterOption(
        field = field,
        values = values,
        toQueryValue = { it },
        fromQueryValue = { it },
        getFilter = getFilter
    )
}

inline fun <reified E : Enum<E>> enumFilterOption(
    field: String,
    noinline getFilter: (Collection<FieldFilter<E>>) -> Filter? = QueryFilterOption.OR
): QueryFilterOption<E> {
    return QueryFilterOption(
        field = field,
        values = enumValues<E>().toList(),
        toQueryValue = { it.name.lowercase().replace("_", "-") },
        fromQueryValue = { enumValueOf<E>(it.uppercase().replace("-", "_")) },
        getFilter = getFilter
    )
}

fun intFilterOption(
    field: String,
    getFilter: (Collection<FieldFilter<*>>) -> Filter? = QueryFilterOption.AND
): QueryFilterOption<Int> {
    return QueryFilterOption(
        field = field,
        values = emptyList(),
        toQueryValue = { it.toString() },
        fromQueryValue = { it.toInt() },
        getFilter = getFilter
    )
}
