package invirt.data

import com.sun.tools.javac.code.TypeAnnotationPosition.field
import invirt.data.DataFilter.Field
import invirt.data.geo.GeoBoundingBox
import kotlin.reflect.KProperty

/**
 * A filter that can be applied to data queries, allowing for complex filtering operations.
 * It supports field-based filters, logical combinations (AND/OR), and geo-bound checks.
 */
sealed interface DataFilter {

    sealed class Field : DataFilter {
//        fun <R : Any> map(convert: (Value) -> R): Field<R> = Field(
//            field = field,
//            operation = operation,
//            value = convert(value)
//        )

        data class Eq<Value : Any>(val field: String, val value: Value) : Field()
        data class Ne<Value : Any>(val field: String, val value: Value) : Field()
        data class Gt<Value : Any>(val field: String, val value: Value) : Field()
        data class Gte<Value : Any>(val field: String, val value: Value) : Field()
        data class Lt<Value : Any>(val field: String, val value: Value) : Field()
        data class Lte<Value : Any>(val field: String, val value: Value) : Field()
        data class WithinGeoBounds(val field: String, val value: GeoBoundingBox) : Field()
        data class Exists(val field: String) : Field()
        data class DoesntExist(val field: String) : Field()
    }

    data class Or(val filters: List<DataFilter>) : DataFilter {
        init {
            if (filters.isEmpty()) {
                throw IllegalArgumentException("Filter collection cannot be empty for an OR filter")
            }
        }
    }

    data class And(val filters: List<DataFilter>) : DataFilter {
        init {
            if (filters.isEmpty()) {
                throw IllegalArgumentException("Filter collection cannot be empty for an AND filter")
            }
        }
    }

    companion object {
        fun or(filters: List<DataFilter>) = Or(filters)
        fun or(vararg filters: DataFilter) = or(filters.toList())
        fun and(filters: List<DataFilter>) = And(filters)
        fun and(vararg filters: DataFilter) = and(filters.toList())
        fun <Value : Any> eq(field: String, value: Value): Field = Field.Eq(field, value)
        fun <Value : Any> ne(field: String, value: Value): Field = Field.Ne(field, value)
        fun <Value : Any> gt(field: String, value: Value): Field = Field.Gt(field, value)
        fun <Value : Any> gte(field: String, value: Value): Field = Field.Gte(field, value)
        fun <Value : Any> lt(field: String, value: Value): Field = Field.Lt(field, value)
        fun <Value : Any> lte(field: String, value: Value): Field = Field.Lte(field, value)
        fun exists(field: String): Field = Field.Exists(field)
        fun doesntExist(field: String): Field = Field.DoesntExist(field)
        fun withingGeoBounds(field: String, value: GeoBoundingBox): Field.WithinGeoBounds =
            Field.WithinGeoBounds(field, value)
    }
}

fun andFilter(vararg filters: DataFilter): DataFilter = DataFilter.And(filters.toList())
fun andFilter(filters: Collection<DataFilter>): DataFilter = DataFilter.And(filters.toList())
fun orFilter(vararg filters: DataFilter): DataFilter = DataFilter.Or(filters.toList())
fun orFilter(filters: Collection<DataFilter>): DataFilter = DataFilter.Or(filters.toList())

infix fun <Value : Any> String.eq(value: Value) = Field.Eq(this, value)
infix fun <Value : Any> String.ne(value: Value) = Field.Ne(this, value)
infix fun <Value : Any> String.gt(value: Value) = Field.Gt(this, value)
infix fun <Value : Any> String.gte(value: Value) = Field.Gte(this, value)
infix fun <Value : Any> String.lt(value: Value) = Field.Lt(this, value)
infix fun <Value : Any> String.lte(value: Value) = Field.Lte(this, value)
infix fun String.withinGeoBounds(value: GeoBoundingBox) = Field.WithinGeoBounds(this, value)
fun String.exists() = Field.Exists(this)
fun String.doesntExist() = Field.DoesntExist(this)

infix fun <Value : Any> KProperty<Value?>.eq(value: Value) = Field.Eq(this.name, value)
infix fun <Value : Any> KProperty<Value?>.ne(value: Value) = Field.Ne(this.name, value)
infix fun <Value : Any> KProperty<Value?>.gt(value: Value) = Field.Gt(this.name, value)
infix fun <Value : Any> KProperty<Value?>.gte(value: Value) = Field.Gte(this.name, value)
infix fun <Value : Any> KProperty<Value?>.lt(value: Value) = Field.Lt(this.name, value)
infix fun <Value : Any> KProperty<Value?>.lte(value: Value) = Field.Lte(this.name, value)
fun KProperty<*>.exists() = Field.Exists(this.name)
fun KProperty<*>.doesntExist() = Field.DoesntExist(this.name)

/**
 * A recursive function to flatten a [DataFilter.Or] and [DataFilter.And] when these contain
 * a single filter. This is useful to simplify the filter structure and complexity.
 */
fun DataFilter.flatten(): DataFilter = when {
    this is DataFilter.Or && filters.size == 1 -> filters.first().flatten()
    this is DataFilter.Or && filters.size > 1 -> DataFilter.Or(filters.map { it.flatten() })
    this is DataFilter.And && filters.size == 1 -> filters.first().flatten()
    this is DataFilter.And && filters.size > 1 -> DataFilter.And(filters.map { it.flatten() })
    else -> this
}
