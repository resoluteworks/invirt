package invirt.http4k.security

import invirt.http4k.security.authentication.Principal
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler

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
                if (Principal.present && check(Principal.current)) {
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
fun authenticatedRoutes(route: RoutingHttpHandler): RoutingHttpHandler {
    val filter =
        Filter { next ->
            { request ->
                if (Principal.present) {
                    next(request)
                } else {
                    Response(Status.FORBIDDEN)
                }
            }
        }
    return filter.then(route)
}
