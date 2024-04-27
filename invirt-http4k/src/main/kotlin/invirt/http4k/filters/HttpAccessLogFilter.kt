package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Status

object HttpAccessLogFilter {

    internal val log = KotlinLogging.logger {}

    operator fun invoke(vararg excludeStatuses: Status = arrayOf(Status.OK)): Filter {
        return invoke(excludeStatuses.toSet())
    }

    operator fun invoke(excludeStatuses: Set<Status> = setOf(Status.OK)): Filter = Filter { next ->
        { request ->
            next(request).also { response ->
                if (response.status !in excludeStatuses) {
                    log.info { "${request.method} ${request.uri} ${response.status.code}" }
                }
            }
        }
    }
}
