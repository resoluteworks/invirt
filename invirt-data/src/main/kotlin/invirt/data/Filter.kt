package invirt.data

interface Filter

data class FieldFilter<Value : Any>(
    val field: String,
    val operation: Operation,
    val value: Value
) : Filter {

    enum class Operation {
        EQ,
        NE,
        GT,
        GTE,
        LT,
        LTE
    }

    fun <R : Any> map(convert: (Value) -> R): FieldFilter<R> {
        return FieldFilter(
            field = field,
            operation = operation,
            value = convert(value)
        )
    }

    companion object {
        fun <Value : Any> eq(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.EQ, value)
        fun <Value : Any> ne(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.NE, value)
        fun <Value : Any> gt(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.GT, value)
        fun <Value : Any> gte(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.GTE, value)
        fun <Value : Any> lt(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.LT, value)
        fun <Value : Any> lte(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.LTE, value)

        fun of(field: String, operationAndValue: String): FieldFilter<String> {
            return if (operationAndValue.contains(":")) {
                val operation = operationAndValue.substringBefore(":")
                val valueString = operationAndValue.substringAfter(":")
                FieldFilter(field, Operation.valueOf(operation.uppercase()), valueString)
            } else {
                FieldFilter(field, Operation.EQ, operationAndValue)
            }
        }
    }
}

data class CompoundFilter(
    val operator: Operator,
    val children: Collection<Filter>
) : Filter {

    enum class Operator {
        OR,
        AND
    }

    companion object {
        fun or(vararg filters: Filter): CompoundFilter = or(filters.toList())

        fun or(filters: List<Filter>): CompoundFilter {
            return CompoundFilter(Operator.OR, filters)
        }

        fun and(vararg filters: Filter): CompoundFilter = and(filters.toList())

        fun and(filters: List<Filter>): CompoundFilter {
            return CompoundFilter(Operator.AND, filters)
        }
    }
}
