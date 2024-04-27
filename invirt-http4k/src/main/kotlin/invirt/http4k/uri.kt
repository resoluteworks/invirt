package invirt.http4k

import invirt.data.Page
import org.http4k.core.Uri
import org.http4k.core.queries
import org.http4k.core.query
import org.http4k.core.toParameters
import org.http4k.core.toUrlFormEncoded

fun Uri.removeQueryValue(name: String, value: Any): Uri {
    return copy(query = query.toParameters().filterNot { it.first == name && it.second.toString() == value }.toUrlFormEncoded())
}

fun Uri.toggleQueryValue(name: String, value: Any): Uri {
    val query = queries().firstOrNull { it.first == name && it.second.toString() == value }
    return query
        ?.let { this.removeQueryValue(name, value) }
        ?: query(name, value.toString())
}

fun Uri.removeQueries(names: Collection<String>): Uri {
    return copy(query = query.toParameters().filterNot { it.first in names }.toUrlFormEncoded())
}

fun Uri.replacePage(page: Page): Uri {
    return replaceQuery(
        "from" to page.from,
        "size" to page.size,
    )
}

fun Uri.replaceQuery(vararg queryValues: Pair<String, Any>): Uri {
    return replaceQuery(mapOf(*queryValues))
}

fun Uri.replaceQuery(queries: Map<String, Any>): Uri {
    var uri = removeQueries(queries.keys)
    queries.forEach { name, value ->
        uri = uri.query(name, value.toString())
    }
    return uri
}
