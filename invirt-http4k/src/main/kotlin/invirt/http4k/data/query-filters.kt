package invirt.http4k.data

import invirt.data.CompoundCriteria

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
