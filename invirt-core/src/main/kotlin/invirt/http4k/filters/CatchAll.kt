package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import kotlin.reflect.KClass

private val log = KotlinLogging.logger {}

/**
 * An http4k filter that catches exceptions thrown by the next filter in the chain and returns a response with the
 * appropriate status code.
 *
 * When an exception is caught, the filter logs the exception message at the error level. When the exception is not
 * mapped to a status code, the filter returns a 500 Internal Server Error response.
 */
object CatchAll {

    operator fun invoke(vararg exceptionStatusMappings: Pair<KClass<out Throwable>, Status>): Filter =
        invoke(exceptionStatusMappings.toMap())

    operator fun invoke(exceptionStatusMappings: Map<KClass<out Throwable>, Status>): Filter = Filter { next ->
        { request ->
            try {
                next(request)
            } catch (t: Throwable) {
                log.error(t) { t.message }
                exceptionStatusMappings[t::class]
                    ?.let { Response(it) }
                    ?: Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
