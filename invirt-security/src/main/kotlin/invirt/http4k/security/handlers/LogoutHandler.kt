package invirt.http4k.security.handlers

import invirt.http4k.httpSeeOther
import invirt.http4k.security.authentication.Authenticator
import invirt.http4k.security.authentication.authentication
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object LogoutHandler {

    operator fun invoke(
        authenticator: Authenticator,
        redirect: String,
        uri: String = "/logout",
        method: Method = Method.GET
    ): RoutingHttpHandler {
        return routes(
            uri bind method to { request ->
                authenticator.logout(request)
                request.authentication = null
                httpSeeOther(redirect)
            }
        )
    }
}
