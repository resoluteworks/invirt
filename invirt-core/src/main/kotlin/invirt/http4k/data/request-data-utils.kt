package invirt.http4k.data

import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import org.http4k.core.Request
import org.http4k.lens.Query
import org.http4k.lens.int

private val fromQuery = Query.int().optional("from")
private val sizeQuery = Query.int().optional("size")
internal val sortQuery = Query.optional("sort")

/**
 * Returns a [Page] object from this request's query parameters `from` and `size`.
 * When any of the parameters are missing, the default values are used.
 *
 * @param defaultFrom The [Page.from] to return when `from` query parameter. Defaults to 0
 * @param defaultSize The [Page.size] to return when `size` query parameter. Defaults to 10
 * @param maxSize Maximum size allowed to be passed via the `size` query parameter. Any value greater
 * than this will return a [Page.size] of [maxSize]. Defaults to 10.
 */
fun Request.page(defaultFrom: Int = 0, defaultSize: Int = 10, maxSize: Int = defaultSize): Page {
    val from = fromQuery(this)
    val size = sizeQuery(this)
    return Page(
        from = from ?: defaultFrom,
        size = (size ?: defaultSize).coerceAtMost(maxSize)
    )
}

/**
 * Returns a [Sort] object from a query param `sort` on this request if present, `null` otherwise.
 * The sort query parameter must be in the form `sort=<field>:<order>` (order casing is ignored):
 *    - /test?sort=name:Asc
 *    - /test?sort=name:desc
 */
fun Request.sort(): Sort? {
    val sortStr = sortQuery(this) ?: return null
    val elements = sortStr.split(":")
    if (elements.size != 2) {
        return Sort(elements.first(), SortOrder.ASC)
    }
    return Sort(elements[0], SortOrder.valueOf(elements[1].uppercase()))
}
