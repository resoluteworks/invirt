package invirt.core.handlers

import invirt.core.GET
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.json
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

/**
 * A simple health check handler that returns a JSON response with a status of "healthy".
 */
object HealthCheck {

    fun json(): RoutingHttpHandler = routes(
        "/health" GET {
            Response(Status.OK).json(HealthStatus())
        }
    )
}

data class HealthStatus(val status: String = "healthy")
