package invirt.http4k

import invirt.data.Page
import org.http4k.core.Uri
import org.http4k.core.queries
import org.http4k.core.query
import org.http4k.core.toParameters
import org.http4k.core.toUrlFormEncoded

/**
 * Checks whether this URI has a query parameter with the specified [name] and [value]
 *
 * @param name Query parameter name
 * @param value Query parameter value
 * @return Returns true when the URI has a query parameter with the specified [name] and [value], false otherwise.
 */
fun Uri.hasQueryValue(name: String, value: String): Boolean = this.queries().any { it.first == name && it.second == value }

fun Uri.removeQueryValue(name: String, value: Any): Uri = copy(
    query = query.toParameters().filterNot {
        it.first == name && it.second.toString() == value
    }.toUrlFormEncoded()
)

fun Uri.toggleQueryValue(name: String, value: Any): Uri {
    val query = queries().firstOrNull { it.first == name && it.second.toString() == value }
    return query
        ?.let { this.removeQueryValue(name, value) }
        ?: query(name, value.toString())
}

fun Uri.removeQueries(names: Collection<String>): Uri = copy(
    query = query.toParameters().filterNot {
        it.first in names
    }.toUrlFormEncoded()
)

fun Uri.replacePage(page: Page): Uri = replaceQuery(
    "from" to page.from,
    "size" to page.size
)

fun Uri.replaceQuery(vararg queryValues: Pair<String, Any>): Uri = replaceQuery(mapOf(*queryValues))

fun Uri.replaceQuery(queries: Map<String, Any>): Uri {
    var uri = removeQueries(queries.keys)
    queries.forEach { name, value ->
        uri = uri.query(name, value.toString())
    }
    return uri
}
