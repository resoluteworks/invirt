package examples.authentication.handlers

import invirt.core.GET
import invirt.core.httpSeeOther
import invirt.security.authentication.principal
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object IndexHandler {

    /**
     * When accessing the default route (/) redirect to /dashboard if there's a user
     * present on the request, otherwise redirect to /login
     */
    operator fun invoke(): RoutingHttpHandler = routes(
        "/" GET { request ->
            if (request.principal != null) {
                httpSeeOther("/dashboard")
            } else {
                httpSeeOther("/login")
            }
        }
    )
}
