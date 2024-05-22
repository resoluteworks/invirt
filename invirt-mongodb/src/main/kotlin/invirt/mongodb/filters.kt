package invirt.mongodb

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import java.time.LocalDate
import kotlin.reflect.KProperty

fun mongoById(id: String) = Filters.eq("_id", id)
fun mongoByIds(vararg ids: String) = Filters.`in`("_id", *ids)
fun mongoByIds(ids: Collection<String>) = mongoByIds(*ids.toTypedArray())

fun <Value : Any> KProperty<Value?>.mongoEq(value: Value): Bson = Filters.eq(this.name, value)
fun <Value : Any> KProperty<Value?>.mongoGt(value: Value): Bson = Filters.gt(this.name, value)
fun <Value : Any> KProperty<Value?>.mongoGte(value: Value): Bson = Filters.gte(this.name, value)
fun <Value : Any> KProperty<Value?>.mongoLt(value: Value): Bson = Filters.lt(this.name, value)
fun <Value : Any> KProperty<Value?>.mongoLte(value: Value): Bson = Filters.lte(this.name, value)
fun <Value : Any> KProperty<Value?>.mongoIn(vararg values: Value): Bson? = this.name.mongoIn(values.toList())
fun <Value : Any> KProperty<Value?>.mongoIn(values: Collection<Value>): Bson? = this.name.mongoIn(values)
fun String.mongoIn(values: Collection<Any>): Bson? = if (values.isNotEmpty()) Filters.`in`(this, *values.toTypedArray()) else null
fun String.mongoIn(vararg values: Any): Bson? = if (values.isNotEmpty()) Filters.`in`(this, *values) else null
fun mongoTextSearch(text: String): Bson = Filters.text(text)

fun KProperty<LocalDate>.inYear(year: Int): Bson {
    return Filters.and(mongoGte(LocalDate.of(year, 1, 1)), mongoLte(LocalDate.of(year, 12, 31)))
}
