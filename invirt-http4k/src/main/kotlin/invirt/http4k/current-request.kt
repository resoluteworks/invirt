package invirt.http4k

import org.http4k.core.Filter
import org.http4k.core.Request

internal val requestThreadLocal = ThreadLocal<Request>()
val currentHttp4kRequest: Request? get() = requestThreadLocal.get()

/**
 * Stores the current http4k request on a thread local. The request can then be accessed with [currentHttp4kRequest]
 */
object StoreRequestOnThread {
    operator fun invoke(): Filter {
        return requestScopeValue(requestThreadLocal) { it }
    }
}
