package invirt.data

data class Filter<Value : Any>(
    val field: String,
    val operation: Operation,
    val value: Value
) {
    enum class Operation {
        EQ,
        GT,
        GTE,
        LT,
        LTE,
        NE
    }

    companion object {
        fun <Value : Any> eq(field: String, value: Value): Filter<Value> = Filter(field, Operation.EQ, value)
        fun <Value : Any> gt(field: String, value: Value): Filter<Value> = Filter(field, Operation.GT, value)
        fun <Value : Any> gte(field: String, value: Value): Filter<Value> = Filter(field, Operation.GTE, value)
        fun <Value : Any> lt(field: String, value: Value): Filter<Value> = Filter(field, Operation.LT, value)
        fun <Value : Any> lte(field: String, value: Value): Filter<Value> = Filter(field, Operation.LTE, value)
        fun <Value : Any> ne(field: String, value: Value): Filter<Value> = Filter(field, Operation.NE, value)

        fun of(field: String, operationAndValue: String): Filter<String> {
            return if (operationAndValue.contains(":")) {
                val operation = operationAndValue.substringBefore(":")
                val valueString = operationAndValue.substringAfter(":")
                Filter(field, Operation.valueOf(operation.uppercase()), valueString)
            } else {
                Filter(field, Operation.EQ, operationAndValue)
            }
        }
    }
}
