package invirt.mongodb

import com.mongodb.client.model.Sorts
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

fun KProperty<*>.mongoSortAsc(): Bson = Sorts.ascending(this.name)
fun String.mongoSortAsc(): Bson = Sorts.ascending(this)
fun KProperty<*>.mongoSortDesc(): Bson = Sorts.descending(this.name)
fun String.mongoSortDesc(): Bson = Sorts.descending(this)
