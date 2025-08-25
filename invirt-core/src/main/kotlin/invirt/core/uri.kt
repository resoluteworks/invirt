package invirt.core

import invirt.data.Page
import invirt.data.Sort
import org.http4k.core.Uri
import org.http4k.core.queries
import org.http4k.core.query
import org.http4k.core.removeQuery
import org.http4k.core.toParameters
import org.http4k.core.toUrlFormEncoded

fun Uri.queryValue(name: String): String? = queries().firstOrNull { it.first.equals(name, ignoreCase = true) }?.second

fun Uri.hasQueryParam(name: String): Boolean = this.queries().any { it.first.equals(name, ignoreCase = true) }
fun Uri.hasQueryValue(name: String, value: String): Boolean = this.queries().any {
    it.first.equals(name, ignoreCase = true) && it.second.equals(value, ignoreCase = true)
}

fun Uri.removeQueryValue(name: String, value: Any): Uri = copy(
    query = query.toParameters().filterNot {
        it.first.equals(name, ignoreCase = true) && it.second.toString().equals(value.toString(), ignoreCase = true)
    }.toUrlFormEncoded()
)

fun Uri.toggleQueryValue(name: String, value: Any): Uri {
    val query = queries().firstOrNull {
        it.first.equals(name, ignoreCase = true) && it.second.toString().equals(value.toString(), ignoreCase = true)
    }
    return query
        ?.let { this.removeQueryValue(name, value) }
        ?: query(name, value.toString())
}

fun Uri.removeQueries(names: Collection<String>): Uri {
    val lowercaseNames = names.map { name -> name.lowercase() }
    return copy(
        query = query.toParameters()
            .filterNot { it.first.lowercase() in lowercaseNames }
            .toUrlFormEncoded()
    )
}

fun Uri.replacePage(page: Page): Uri = replaceQuery(
    "from" to page.from,
    "size" to page.size
)

fun Uri.replaceSort(sort: Sort, resetPagination: Boolean = true): Uri {
    val uri = replaceQuery("sort" to sort.toString())
    return if (resetPagination) {
        uri.removeQueries(listOf("from", "size"))
    } else {
        uri
    }
}

fun Uri.replaceQuery(vararg queryValues: Pair<String, Any>): Uri = replaceQuery(mapOf(*queryValues))

fun Uri.replaceQuery(queries: Map<String, Any>): Uri {
    var uri = removeQueries(queries.keys)
    queries.forEach { (name, value) ->
        uri = uri.query(name, value.toString())
    }
    return uri
}

fun Uri.csvQuery(name: String): List<String> = queryValue(name)?.split(",") ?: emptyList()

fun Uri.csvAppend(name: String, value: Any): Uri = replaceQuery(
    name to csvQuery(name).plus(value).toSet().joinToString(",")
)

fun Uri.csvRemove(name: String, value: Any): Uri {
    val values = csvQuery(name).toMutableSet().filter { it.lowercase() != value.toString().lowercase() }
    return if (values.isEmpty()) {
        removeQuery(name)
    } else {
        replaceQuery(name to values.joinToString(","))
    }
}

fun Uri.csvToggle(name: String, value: Any): Uri {
    val values = csvQuery(name)
    return if (values.contains(value.toString())) {
        csvRemove(name, value)
    } else {
        csvAppend(name, value)
    }
}
