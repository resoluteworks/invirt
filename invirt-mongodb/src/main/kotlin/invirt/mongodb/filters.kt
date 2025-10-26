package invirt.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Polygon
import com.mongodb.client.model.geojson.Position
import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import org.bson.conversions.Bson
import java.time.LocalDate
import kotlin.reflect.KProperty

fun mongoById(id: String) = Filters.eq("_id", id)
fun mongoByIds(ids: Collection<String>) = Filters.`in`("_id", ids)
fun mongoByIds(vararg ids: String) = mongoByIds(ids.toList())

fun KProperty<*>.mongoEq(value: Any): Bson = Filters.eq(this.name, value)
fun KProperty<*>.mongoGt(value: Any): Bson = Filters.gt(this.name, value)
fun KProperty<*>.mongoGte(value: Any): Bson = Filters.gte(this.name, value)
fun KProperty<*>.mongoLt(value: Any): Bson = Filters.lt(this.name, value)
fun KProperty<*>.mongoLte(value: Any): Bson = Filters.lte(this.name, value)
fun KProperty<*>.mongoIn(vararg values: Any): Bson = this.name.mongoIn(values.toList())
fun KProperty<*>.mongoIn(values: Collection<Any>): Bson = this.name.mongoIn(values)
fun KProperty<*>.mongoExists(): Bson = this.name.mongoExists()

fun String.mongoEq(value: Any): Bson = Filters.eq(this, value)
fun String.mongoGt(value: Any): Bson = Filters.gt(this, value)
fun String.mongoGte(value: Any): Bson = Filters.gte(this, value)
fun String.mongoLt(value: Any): Bson = Filters.lt(this, value)
fun String.mongoLte(value: Any): Bson = Filters.lte(this, value)
fun String.mongoIn(vararg values: Any): Bson = mongoIn(values.toList())
fun String.mongoExists(): Bson = Filters.exists(this)

fun String.mongoIn(values: Collection<Any>): Bson {
    if (values.isEmpty()) {
        throw IllegalArgumentException("Values for mongoIn cannot be empty")
    }
    return Filters.`in`(this, values)
}

fun mongoTextSearch(text: String): Bson = Filters.text(text)

fun KProperty<LocalDate>.inYear(year: Int): Bson = Filters.and(mongoGte(LocalDate.of(year, 1, 1)), mongoLte(LocalDate.of(year, 12, 31)))

fun GeoLocation.toPosition(): Position = Position(lng, lat)

fun KProperty<GeoLocation?>.mongoGeoBounds(geoBounds: GeoBoundingBox): Bson = this.name.mongoGeoBounds(geoBounds)

fun String.mongoGeoBounds(geoBounds: GeoBoundingBox): Bson {
    val positions = geoBounds.points.plus(geoBounds.southWest).map { it.toPosition() }
    return Filters.geoWithin(this, Polygon(positions))
}
