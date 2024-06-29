package invirt.http4k

import invirt.pebble.InvirtPebbleRequest
import invirt.utils.withValue
import io.validk.ValidationErrors
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey

object InvirtRequestContext {

    val requestContexts = RequestContexts()
    private val requestThreadLocal = ThreadLocal<Request>()
    private val validationErrorContextKey = RequestContextKey.optional<ValidationErrors>(requestContexts)

    operator fun invoke(): Filter {
        val storeRequestOnCurrentThread = Filter { next ->
            { request ->
                requestThreadLocal.withValue(InvirtPebbleRequest(request)) {
                    next(request)
                }
            }
        }
        return ServerFilters.InitialiseRequestContext(requestContexts)
            .then(storeRequestOnCurrentThread)
    }

    internal fun setErrors(errors: ValidationErrors) {
        validationErrorContextKey[requestThreadLocal.get()] = errors
    }

    val currentRequest: Request? get() = requestThreadLocal.get()
    val errors: ValidationErrors? get() = requestThreadLocal.get()?.let { validationErrorContextKey[it] }
}
