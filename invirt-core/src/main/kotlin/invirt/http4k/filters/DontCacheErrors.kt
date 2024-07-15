package invirt.http4k.filters

import org.http4k.core.Filter
import org.http4k.core.Status
import org.http4k.core.noCache

/**
 * Disable caching for certain error statuses
 */
object DontCacheErrors {

    operator fun invoke(vararg errorStatuses: Status = arrayOf(Status.NOT_FOUND)): Filter {
        return invoke(errorStatuses.toSet())
    }

    operator fun invoke(errorStatuses: Set<Status> = setOf(Status.NOT_FOUND)): Filter = Filter { next ->
        { request ->
            val response = next(request)
            if (response.status in errorStatuses) {
                response.noCache()
            } else {
                response
            }
        }
    }
}
