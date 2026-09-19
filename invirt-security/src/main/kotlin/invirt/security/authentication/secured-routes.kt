package invirt.security.authentication

import invirt.core.httpSeeOther
import invirt.core.turboStreamRedirect
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

/**
 * Used to secure routes with authentication/authorisation checks
 */
inline fun <reified P : Principal> securedRoutes(
    crossinline check: (P) -> Boolean,
    route: RoutingHttpHandler
): RoutingHttpHandler {
    val filter =
        Filter { next ->
            { request ->
                if (request.principal != null && check(request.principal as P)) {
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
                if (request.principal != null) {
                    next(request)
                } else {
                    Response(Status.FORBIDDEN)
                }
            }
        }
    return filter.then(routes(routes.toList()))
}

/**
 * Filter that sits in front of a [securedRoutes] or [authenticatedRoutes] block and turns an
 * anonymous navigational GET into a redirect to a sign-in page, instead of the bare
 * [Status.FORBIDDEN] those two answer with - a browser app almost always wants the visitor bounced
 * to sign-in and back, not a dead end.
 *
 * [signInUrl] builds the redirect target from the incoming request. This library has no opinion on
 * where an application's sign-in page lives or what it needs on its query string (a return
 * destination, a sign-up intent, anything else) - that is entirely the caller's to encode.
 *
 * A Turbo-Frame request (one carrying a `Turbo-Frame` header) gets a [turboStreamRedirect] rather
 * than a plain redirect, so the top-level page navigates instead of the frame trying to load the
 * sign-in page inline. Non-GET requests and requests that already carry a principal pass straight
 * through to `next`, typically the [securedRoutes] or [authenticatedRoutes] check that follows.
 */
fun redirectAnonymousToSignIn(signInUrl: (Request) -> String): Filter = Filter { next ->
    { request ->
        if (request.principal == null && request.method == Method.GET) {
            if (request.header("Turbo-Frame") != null) {
                turboStreamRedirect(signInUrl(request))
            } else {
                httpSeeOther(signInUrl(request))
            }
        } else {
            next(request)
        }
    }
}
