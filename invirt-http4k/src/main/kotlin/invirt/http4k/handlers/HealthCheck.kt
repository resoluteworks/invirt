package invirt.http4k.handlers

import invirt.http4k.GET
import invirt.http4k.jsonLens
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object HealthCheck {

    internal val jsonLens = jsonLens<HealthStatus>()

    val json: RoutingHttpHandler = routes(
        "/health" GET {
            Response(Status.OK).with(jsonLens of HealthStatus())
        }
    )
}

data class HealthStatus(val status: String = "healthy")
