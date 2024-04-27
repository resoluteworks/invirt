package invirt.http4k

import invirt.utils.withValue
import org.http4k.core.Filter
import org.http4k.core.Request

/**
 * Creates a filter that extracts a value from the current [org.http4k.core.Request] using the [value] lambda
 * and stores it against the specified [threadLocal]. The thread local value is then cleared after the request has been handled.
 */
fun <T> requestScopeValue(threadLocal: ThreadLocal<T>, value: (Request) -> T): Filter {
    return Filter { next ->
        { request ->
            threadLocal.withValue(value(request)) {
                next(request)
            }
        }
    }
}
