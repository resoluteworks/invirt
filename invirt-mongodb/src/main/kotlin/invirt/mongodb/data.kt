package invirt.mongodb

import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.FindIterable
import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import org.bson.conversions.Bson

fun <T : Any> FindIterable<T>.page(page: Page): FindIterable<T> {
    return this.skip(page.from)
        .limit(page.size)
}

fun Sort.mongoSort(): Bson {
    return when (order) {
        SortOrder.ASC -> Sorts.ascending(field)
        SortOrder.DESC -> Sorts.descending(field)
    }
}

fun List<Sort>.mongoSort(): Bson? {
    return if (this.isNotEmpty()) {
        Sorts.orderBy(map { it.mongoSort() })
    } else {
        null
    }
}

fun Array<out Sort>.mongoSort(): Bson? {
    return if (this.isNotEmpty()) {
        Sorts.orderBy(map { it.mongoSort() })
    } else {
        null
    }
}

fun <T : Any> FindIterable<T>.sort(vararg sort: Sort = emptyArray()): FindIterable<T> {
    return if (sort.isNotEmpty()) {
        this.sort(sort.toList().mongoSort())
    } else {
        this
    }
}
