package invirt.http4k

import org.http4k.core.Filter
import org.http4k.core.RequestContexts
import org.http4k.filter.ServerFilters

object AppRequestContexts {
    val contexts = RequestContexts()

    operator fun invoke(): Filter = ServerFilters.InitialiseRequestContext(contexts)
}
