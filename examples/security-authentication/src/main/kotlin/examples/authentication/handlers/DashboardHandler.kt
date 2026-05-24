package examples.authentication.handlers

import invirt.core.GET
import invirt.core.views.renderTemplate
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object DashboardHandler {

    operator fun invoke(): RoutingHttpHandler = routes(
        "/dashboard" GET { request -> renderTemplate(request, "dashboard") }
    )
}
