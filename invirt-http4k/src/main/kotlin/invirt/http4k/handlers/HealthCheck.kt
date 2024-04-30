package invirt.http4k.handlers

import invirt.http4k.GET
import invirt.http4k.jsonLens
import invirt.http4k.ok
import invirt.http4k.views.ok
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object HealthCheck {

    internal val jsonLens = jsonLens<HealthStatus>()

    val json: RoutingHttpHandler = routes(
        "/health" GET {
            HealthStatus().ok(jsonLens)
        }
    )
}

data class HealthStatus(val status: String = "healthy")
