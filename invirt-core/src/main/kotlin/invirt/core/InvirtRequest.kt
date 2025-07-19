package invirt.core

import invirt.core.data.sort
import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import org.http4k.core.Request
import org.http4k.core.Uri

/**
 * A thin wrapper of http4k's [Request] object.
 * @param delegate The underlying http4k [Request]
 */
class InvirtRequest(private val delegate: Request) : Request by delegate {

    val sort: Sort? = delegate.sort()

    fun host(): String? = header("Host")

    fun hasQueryParam(name: String): Boolean = delegate.uri.hasQueryParam(name)
    fun hasQueryValue(name: String, value: String): Boolean = delegate.uri.hasQueryValue(name, value)
    fun toggleQueryValue(name: String, value: Any): Uri = delegate.uri.toggleQueryValue(name, value)
    fun replacePage(page: Page): Uri = delegate.uri.replacePage(page)
    fun replaceQuery(name: String, value: Any): Uri = delegate.uri.replaceQuery(name to value)
    fun removeQueryValue(name: String, value: Any): Uri = delegate.uri.removeQueryValue(name, value)
    fun removeQueries(names: Collection<String>): Uri = delegate.uri.removeQueries(names)
    fun csvAppend(name: String, value: Any): Uri = delegate.uri.csvAppend(name, value)
    fun csvRemove(name: String, value: Any): Uri = delegate.uri.csvRemove(name, value)
    fun csvToggle(name: String, value: Any): Uri = delegate.uri.csvToggle(name, value)

    fun replaceSort(field: String, orderStr: String, resetPagination: Boolean): Uri =
        delegate.uri.replaceSort(Sort(field, SortOrder.fromString(orderStr)), resetPagination)

    fun revertOrSetSort(field: String, orderStr: String, resetPagination: Boolean): Uri {
        val sort = this.sort()
        return if (sort == null || sort.field != field) {
            delegate.uri.replaceSort(Sort(field, SortOrder.fromString(orderStr)), resetPagination)
        } else {
            delegate.uri.replaceSort(sort.revert(), resetPagination)
        }
    }

    fun sortIs(field: String, orderStr: String): Boolean = Sort(field, SortOrder.fromString(orderStr)) == this.sort()
}
