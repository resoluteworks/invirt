package invirt.http4k

import invirt.data.Page
import org.http4k.core.Request
import org.http4k.core.Uri

class InvirtRequest(val delegate: Request) : Request by delegate {

    fun hasQueryValue(name: String, value: String): Boolean = delegate.uri.hasQueryValue(name, value)
    fun toggleQueryValue(name: String, value: Any): Uri = delegate.uri.toggleQueryValue(name, value)
    fun replacePage(page: Page): Uri = delegate.uri.replacePage(page)
    fun replaceQuery(name: String, value: Any): Uri = delegate.uri.replaceQuery(name to value)
    fun removeQueryValue(name: String, value: Any): Uri = delegate.uri.removeQueryValue(name, value)
    fun removeQueries(names: Collection<String>): Uri = delegate.uri.removeQueries(names)
    fun removeQueries(names: Array<String>): Uri = delegate.uri.removeQueries(names.toSet())
}
