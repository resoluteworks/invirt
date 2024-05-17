package invirt.data

interface FilterCriteria

data class FieldCriteria<Value : Any>(
    val field: String,
    val operation: Operation,
    val value: Value
) : FilterCriteria {

    enum class Operation {
        EQ,
        NE,
        GT,
        GTE,
        LT,
        LTE
    }

    fun <R : Any> map(convert: (Value) -> R): FieldCriteria<R> {
        return FieldCriteria(
            field = field,
            operation = operation,
            value = convert(value)
        )
    }

    companion object {
        fun <Value : Any> eq(field: String, value: Value): FieldCriteria<Value> = FieldCriteria(field, Operation.EQ, value)
        fun <Value : Any> ne(field: String, value: Value): FieldCriteria<Value> = FieldCriteria(field, Operation.NE, value)
        fun <Value : Any> gt(field: String, value: Value): FieldCriteria<Value> = FieldCriteria(field, Operation.GT, value)
        fun <Value : Any> gte(field: String, value: Value): FieldCriteria<Value> = FieldCriteria(field, Operation.GTE, value)
        fun <Value : Any> lt(field: String, value: Value): FieldCriteria<Value> = FieldCriteria(field, Operation.LT, value)
        fun <Value : Any> lte(field: String, value: Value): FieldCriteria<Value> = FieldCriteria(field, Operation.LTE, value)

        fun of(field: String, operationAndValue: String): FieldCriteria<String> {
            return if (operationAndValue.contains(":")) {
                val operation = operationAndValue.substringBefore(":")
                val valueString = operationAndValue.substringAfter(":")
                FieldCriteria(field, Operation.valueOf(operation.uppercase()), valueString)
            } else {
                FieldCriteria(field, Operation.EQ, operationAndValue)
            }
        }
    }
}

data class CompoundCriteria(
    val operator: Operator,
    val children: List<FilterCriteria>
) : FilterCriteria {

    enum class Operator {
        OR,
        AND
    }

    companion object {
        fun or(vararg criteria: FilterCriteria): CompoundCriteria = or(criteria.toList())

        fun or(criteria: List<FilterCriteria>): CompoundCriteria {
            return CompoundCriteria(Operator.OR, criteria)
        }

        fun and(vararg criteria: FilterCriteria): CompoundCriteria = and(criteria.toList())

        fun and(criteria: List<FilterCriteria>): CompoundCriteria {
            return CompoundCriteria(Operator.AND, criteria)
        }
    }
}
