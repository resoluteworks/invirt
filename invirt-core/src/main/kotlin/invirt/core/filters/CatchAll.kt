package invirt.core.filters

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import kotlin.reflect.KClass

/**
 * An http4k filter that catches exceptions thrown by the next filter in the chain and returns a response with the
 * appropriate status code.
 *
 * When an exception is caught, the filter logs it and answers with the status mapped to the exception's class. When the
 * exception is not mapped to a status code, the filter returns a 500 Internal Server Error response.
 *
 * The log event has a constant message, so that events can be grouped on it, and the variable data as payload: the
 * status the filter answers with under `status` and the exception message under `errorMessage`. The exception is the
 * cause of the event, so its stack trace is logged.
 *
 * The level follows the status the filter answers with. A status of 500 or above, which includes the 500 answered for
 * an unmapped exception, is logged at ERROR. Anything below 500 is logged at WARN: an exception the application maps to
 * a 4xx is an expected outcome of a request rather than a failure to serve it, so it stays out of the ERROR events that
 * alerting typically watches.
 */
object CatchAll {

    /** Read through a lazy property so that a test can replace the logger with a mock. */
    internal val logger: KLogger by lazy { KotlinLogging.logger {} }

    operator fun invoke(vararg exceptionStatusMappings: Pair<KClass<out Exception>, Status>): Filter =
        invoke(exceptionStatusMappings.toMap())

    operator fun invoke(exceptionStatusMappings: Map<KClass<out Exception>, Status>): Filter = Filter { next ->
        { request ->
            try {
                next(request)
            } catch (t: Exception) {
                val status = exceptionStatusMappings[t::class] ?: Status.INTERNAL_SERVER_ERROR
                val level = if (status.code >= 500) Level.ERROR else Level.WARN
                logger.at(level) {
                    cause = t
                    message = "Request failed with an exception"
                    payload = mapOf("status" to status.code, "errorMessage" to t.message)
                }
                Response(status)
            }
        }
    }
}
