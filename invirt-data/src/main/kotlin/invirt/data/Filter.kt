package invirt.data

import invirt.data.geo.GeoBoundingBox
import kotlin.reflect.KProperty

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
        LTE,
        WITHIN_GEO_BOUNDS,
        EXISTS,
        DOESNT_EXIST
    }

    fun <R : Any> map(convert: (Value) -> R): FieldFilter<R> = FieldFilter(
        field = field,
        operation = operation,
        value = convert(value)
    )

    companion object {
        fun <Value : Any> eq(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.EQ, value)
        fun <Value : Any> ne(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.NE, value)
        fun <Value : Any> gt(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.GT, value)
        fun <Value : Any> gte(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.GTE, value)
        fun <Value : Any> lt(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.LT, value)
        fun <Value : Any> lte(field: String, value: Value): FieldFilter<Value> = FieldFilter(field, Operation.LTE, value)
        fun exists(field: String): FieldFilter<Unit> = FieldFilter(field, Operation.EXISTS, Unit)
        fun doesntExist(field: String): FieldFilter<Unit> = FieldFilter(field, Operation.DOESNT_EXIST, Unit)

        fun withingGeoBounds(field: String, value: GeoBoundingBox): FieldFilter<GeoBoundingBox> =
            FieldFilter(field, Operation.WITHIN_GEO_BOUNDS, value)
    }
}

data class CompoundFilter(
    val operator: Operator,
    val children: Collection<Filter>
) : Filter {

    init {
        if (children.isEmpty()) {
            throw IllegalArgumentException("children argument cannot be an empty collection")
        }
    }

    enum class Operator {
        OR,
        AND
    }

    companion object {
        fun or(vararg filters: Filter): CompoundFilter = or(filters.toList())

        fun or(filters: Collection<Filter>): CompoundFilter = CompoundFilter(Operator.OR, filters)

        fun and(vararg filters: Filter): CompoundFilter = and(filters.toList())

        fun and(filters: Collection<Filter>): CompoundFilter = CompoundFilter(Operator.AND, filters)
    }
}

fun Collection<Filter>.orFilter(): Filter? = if (isEmpty()) {
    null
} else {
    CompoundFilter.or(this)
}

fun Collection<Filter>.andFilter(): Filter? = if (isEmpty()) {
    null
} else {
    CompoundFilter.and(this)
}

infix fun <Value : Any> String.eq(value: Value): FieldFilter<Value> = FieldFilter.eq(this, value)
infix fun <Value : Any> String.ne(value: Value): FieldFilter<Value> = FieldFilter.ne(this, value)
infix fun <Value : Any> String.gt(value: Value): FieldFilter<Value> = FieldFilter.gt(this, value)
infix fun <Value : Any> String.gte(value: Value): FieldFilter<Value> = FieldFilter.gte(this, value)
infix fun <Value : Any> String.lt(value: Value): FieldFilter<Value> = FieldFilter.lt(this, value)
infix fun <Value : Any> String.lte(value: Value): FieldFilter<Value> = FieldFilter.lte(this, value)
infix fun String.withinGeoBounds(value: GeoBoundingBox): FieldFilter<GeoBoundingBox> = FieldFilter.withingGeoBounds(this, value)
fun String.exists(): FieldFilter<Unit> = FieldFilter.exists(this)
fun String.doesntExist(): FieldFilter<Unit> = FieldFilter.doesntExist(this)

infix fun <Value : Any> KProperty<Value?>.eq(value: Value): FieldFilter<Value> = FieldFilter.eq(this.name, value)
infix fun <Value : Any> KProperty<Value?>.ne(value: Value): FieldFilter<Value> = FieldFilter.ne(this.name, value)
infix fun <Value : Any> KProperty<Value?>.gt(value: Value): FieldFilter<Value> = FieldFilter.gt(this.name, value)
infix fun <Value : Any> KProperty<Value?>.gte(value: Value): FieldFilter<Value> = FieldFilter.gte(this.name, value)
infix fun <Value : Any> KProperty<Value?>.lt(value: Value): FieldFilter<Value> = FieldFilter.lt(this.name, value)
infix fun <Value : Any> KProperty<Value?>.lte(value: Value): FieldFilter<Value> = FieldFilter.lte(this.name, value)
fun KProperty<*>.exists(): FieldFilter<Unit> = FieldFilter.exists(this.name)
fun KProperty<*>.doesntExist(): FieldFilter<Unit> = FieldFilter.doesntExist(this.name)

/**
 * Returns the underlying filter when the or/and clause contains a single filter
 */
fun Filter.flatten(): Filter = if (this is CompoundFilter) {
    if (children.size == 1) {
        children.first().flatten()
    } else {
        CompoundFilter(this.operator, children.map { it.flatten() })
    }
} else {
    this
}
