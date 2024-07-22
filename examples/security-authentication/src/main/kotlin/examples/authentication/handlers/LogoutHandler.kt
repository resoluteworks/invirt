package examples.authentication.handlers

import examples.authentication.service.AuthenticationService
import invirt.http4k.POST
import invirt.http4k.httpSeeOther
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object LogoutHandler {

    operator fun invoke(authService: AuthenticationService): RoutingHttpHandler = routes(
        "/logout" POST { request ->
            authService.invalidateCookies(httpSeeOther("/"))
        }
    )
}
