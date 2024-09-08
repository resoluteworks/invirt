package invirt.core.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Status

private val log = KotlinLogging.logger {}

/**
 * An http4k filter that overrides the status of the response.
 */
object StatusOverride {

    operator fun invoke(vararg overrides: Pair<Status, Status>): Filter = invoke(overrides.toMap())

    operator fun invoke(overrides: Map<Status, Status>): Filter = Filter { next ->
        { request ->
            val response = next(request)
            overrides[response.status]?.let { newStatus ->
                log.debug { "Overriding status for ${request.uri} from ${response.status} to $newStatus" }
                response.status(newStatus)
            } ?: response
        }
    }
}
