package invirt.core

import invirt.utils.threads.withValue
import io.validk.ValidationErrors
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey

object InvirtRequestContext {
    val http4kRequestContexts = RequestContexts()
    private val requestThreadLocal = ThreadLocal<Request>()
    private val validationErrorContextKey = RequestContextKey.optional<ValidationErrors>(http4kRequestContexts)

    internal fun filter(): Filter {
        val storeRequestOnCurrentThread = Filter { next ->
            { request ->
                requestThreadLocal.withValue(InvirtRequest(request)) {
                    next(request)
                }
            }
        }
        return ServerFilters.InitialiseRequestContext(http4kRequestContexts)
            .then(storeRequestOnCurrentThread)
    }

    val request: Request? get() = requestThreadLocal.get()

    internal fun setErrors(errors: ValidationErrors) {
        validationErrorContextKey[requestThreadLocal.get()] = errors
    }

    val errors: ValidationErrors? get() = requestThreadLocal.get()?.let { validationErrorContextKey[it] }
}
