package invirt.data

import invirt.data.geo.GeoBoundingBox
import kotlin.reflect.KProperty

sealed class DataFilter {

    data class Field<Value : Any>(
        val field: String,
        val operation: Operation,
        val value: Value
    ) : DataFilter() {

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

        fun <R : Any> map(convert: (Value) -> R): Field<R> = Field(
            field = field,
            operation = operation,
            value = convert(value)
        )

        companion object {
            fun <Value : Any> eq(field: String, value: Value): Field<Value> = Field(field, Operation.EQ, value)
            fun <Value : Any> ne(field: String, value: Value): Field<Value> = Field(field, Operation.NE, value)
            fun <Value : Any> gt(field: String, value: Value): Field<Value> = Field(field, Operation.GT, value)
            fun <Value : Any> gte(field: String, value: Value): Field<Value> = Field(field, Operation.GTE, value)
            fun <Value : Any> lt(field: String, value: Value): Field<Value> = Field(field, Operation.LT, value)
            fun <Value : Any> lte(field: String, value: Value): Field<Value> = Field(field, Operation.LTE, value)
            fun exists(field: String): Field<Unit> = Field(field, Operation.EXISTS, Unit)
            fun doesntExist(field: String): Field<Unit> = Field(field, Operation.DOESNT_EXIST, Unit)

            fun withingGeoBounds(field: String, value: GeoBoundingBox): Field<GeoBoundingBox> =
                Field(field, Operation.WITHIN_GEO_BOUNDS, value)
        }
    }

    data class Compound(
        val operator: Operator,
        val subFilters: Collection<DataFilter>
    ) : DataFilter() {

        init {
            if (subFilters.isEmpty()) {
                throw IllegalArgumentException("children argument cannot be an empty collection")
            }
        }

        enum class Operator {
            OR,
            AND
        }

        companion object {
            fun or(vararg filters: DataFilter): Compound = or(filters.toList())

            fun or(filters: Collection<DataFilter>): Compound = Compound(Operator.OR, filters)

            fun and(vararg filters: DataFilter): Compound = and(filters.toList())

            fun and(filters: Collection<DataFilter>): Compound = Compound(Operator.AND, filters)
        }
    }
}

fun andFilter(vararg filters: DataFilter): DataFilter = filters.toList().andFilter()
fun orFilter(vararg filters: DataFilter): DataFilter = filters.toList().orFilter()

fun Collection<DataFilter>.orFilter(): DataFilter = if (isEmpty()) {
    throw IllegalArgumentException("Filter colletion cannot be empty for an OR filter")
} else {
    DataFilter.Compound.or(this)
}

fun Collection<DataFilter>.andFilter(): DataFilter = if (isEmpty()) {
    throw IllegalArgumentException("Filter colletion cannot be empty for an AND filter")
} else {
    DataFilter.Compound.and(this)
}

infix fun <Value : Any> String.eq(value: Value): DataFilter.Field<Value> = DataFilter.Field.eq(this, value)
infix fun <Value : Any> String.ne(value: Value): DataFilter.Field<Value> = DataFilter.Field.ne(this, value)
infix fun <Value : Any> String.gt(value: Value): DataFilter.Field<Value> = DataFilter.Field.gt(this, value)
infix fun <Value : Any> String.gte(value: Value): DataFilter.Field<Value> = DataFilter.Field.gte(this, value)
infix fun <Value : Any> String.lt(value: Value): DataFilter.Field<Value> = DataFilter.Field.lt(this, value)
infix fun <Value : Any> String.lte(value: Value): DataFilter.Field<Value> = DataFilter.Field.lte(this, value)
infix fun String.withinGeoBounds(value: GeoBoundingBox): DataFilter.Field<GeoBoundingBox> = DataFilter.Field.withingGeoBounds(this, value)
fun String.exists(): DataFilter.Field<Unit> = DataFilter.Field.exists(this)
fun String.doesntExist(): DataFilter.Field<Unit> = DataFilter.Field.doesntExist(this)

infix fun <Value : Any> KProperty<Value?>.eq(value: Value): DataFilter.Field<Value> = DataFilter.Field.eq(this.name, value)
infix fun <Value : Any> KProperty<Value?>.ne(value: Value): DataFilter.Field<Value> = DataFilter.Field.ne(this.name, value)
infix fun <Value : Any> KProperty<Value?>.gt(value: Value): DataFilter.Field<Value> = DataFilter.Field.gt(this.name, value)
infix fun <Value : Any> KProperty<Value?>.gte(value: Value): DataFilter.Field<Value> = DataFilter.Field.gte(this.name, value)
infix fun <Value : Any> KProperty<Value?>.lt(value: Value): DataFilter.Field<Value> = DataFilter.Field.lt(this.name, value)
infix fun <Value : Any> KProperty<Value?>.lte(value: Value): DataFilter.Field<Value> = DataFilter.Field.lte(this.name, value)
fun KProperty<*>.exists(): DataFilter.Field<Unit> = DataFilter.Field.exists(this.name)
fun KProperty<*>.doesntExist(): DataFilter.Field<Unit> = DataFilter.Field.doesntExist(this.name)

/**
 * Returns the underlying filter when the or/and clause contains a single filter
 */
fun DataFilter.flatten(): DataFilter = if (this is DataFilter.Compound) {
    if (subFilters.size == 1) {
        subFilters.first().flatten()
    } else {
        DataFilter.Compound(this.operator, subFilters.map { it.flatten() })
    }
} else {
    this
}
