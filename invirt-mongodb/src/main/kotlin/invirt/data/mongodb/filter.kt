package invirt.data.mongodb

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import java.time.LocalDate
import kotlin.reflect.KProperty

fun byId(id: String) = Filters.eq("_id", id)
fun byIds(vararg ids: String) = Filters.`in`("_id", *ids)
fun byIds(ids: Collection<String>) = byIds(*ids.toTypedArray())

fun KProperty<*>.eq(value: Any): Bson = Filters.eq(this.name, value)
fun KProperty<*>.gt(value: Any): Bson = Filters.gt(this.name, value)
fun KProperty<*>.gte(value: Any): Bson = Filters.gte(this.name, value)
fun KProperty<*>.lt(value: Any): Bson = Filters.lt(this.name, value)
fun KProperty<*>.lte(value: Any): Bson = Filters.lte(this.name, value)
fun KProperty<*>.`in`(vararg values: Any): Bson? = this.name.`in`(values.toList())
fun KProperty<*>.`in`(values: Collection<Any>): Bson? = this.name.`in`(values)
fun String.`in`(values: Collection<Any>): Bson? = if (values.isNotEmpty()) Filters.`in`(this, *values.toTypedArray()) else null
fun String.`in`(vararg values: Any): Bson? = if (values.isNotEmpty()) Filters.`in`(this, *values) else null
fun textSearch(text: String): Bson = Filters.text(text)

fun KProperty<LocalDate>.inYear(year: Int): Bson {
    return Filters.and(gte(LocalDate.of(year, 1, 1)), lte(LocalDate.of(year, 12, 31)))
}
