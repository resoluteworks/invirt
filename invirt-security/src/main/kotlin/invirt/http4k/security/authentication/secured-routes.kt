package invirt.http4k.security.authentication

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

/**
 * Used to secure routes with authentication/authorisation checks
 */
fun securedRoutes(
    check: (Principal) -> Boolean,
    route: RoutingHttpHandler
): RoutingHttpHandler {
    val filter =
        Filter { next ->
            { request ->
                if (Principal.isPresent && check(Principal.current)) {
                    next(request)
                } else {
                    Response(Status.FORBIDDEN)
                }
            }
        }
    return filter.then(route)
}

/**
 * Used for routes where a principal is required, but no other principal checks are required
 */
fun authenticatedRoutes(vararg routes: RoutingHttpHandler): RoutingHttpHandler {
    val filter =
        Filter { next ->
            { request ->
                if (Principal.isPresent) {
                    next(request)
                } else {
                    Response(Status.FORBIDDEN)
                }
            }
        }
    return filter.then(routes(routes.toList()))
}
