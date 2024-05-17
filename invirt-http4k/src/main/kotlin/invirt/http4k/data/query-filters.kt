package invirt.http4k.data

import invirt.data.CompoundCriteria
import invirt.data.FieldCriteria
import invirt.data.FilterCriteria
import org.http4k.core.Request

class RequestQueryFilters(
    val options: List<QueryFilterOption<*>>,
    val selected: List<FieldCriteria<*>>,
    val criteria: FilterCriteria?
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
        selected = request.selectedFilters(options),
        criteria = request.filterCriteria(options)
    )
}

class QueryFilterOption<Value : Any>(
    val field: String,
    val values: List<Value>,
    val toQueryValue: (Value) -> String,
    val fromQueryValue: (String) -> Value,
    val operator: CompoundCriteria.Operator
)

fun stringFilterOption(
    field: String,
    values: List<String>,
    operator: CompoundCriteria.Operator = CompoundCriteria.Operator.OR
): QueryFilterOption<String> {
    return QueryFilterOption(
        field = field,
        values = values,
        toQueryValue = { it },
        fromQueryValue = { it },
        operator = operator
    )
}

fun intFilterOption(
    field: String,
    operator: CompoundCriteria.Operator = CompoundCriteria.Operator.OR
): QueryFilterOption<Int> {
    return QueryFilterOption(
        field = field,
        values = emptyList(),
        toQueryValue = { it.toString() },
        fromQueryValue = { it.toInt() },
        operator = operator
    )
}

inline fun <reified E : Enum<E>> enumFilterOption(
    field: String,
    operator: CompoundCriteria.Operator = CompoundCriteria.Operator.OR
): QueryFilterOption<E> {
    return QueryFilterOption(
        field = field,
        values = enumValues<E>().toList(),
        toQueryValue = { it.name.lowercase().replace("_", "-") },
        fromQueryValue = { enumValueOf<E>(it.uppercase().replace("-", "_")) },
        operator = operator
    )
}
