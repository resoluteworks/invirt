package examples.authentication.handlers

import invirt.http4k.GET
import invirt.http4k.views.renderTemplate
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object DashboardHandler {

    operator fun invoke(): RoutingHttpHandler = routes(
        "/dashboard" GET { renderTemplate("dashboard") }
    )
}
