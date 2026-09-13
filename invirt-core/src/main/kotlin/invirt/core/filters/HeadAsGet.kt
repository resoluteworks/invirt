package invirt.core.filters

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method

/**
 * Answers HEAD requests by running the handler as GET and discarding the response body.
 *
 * http4k's routing matches the request method by strict equality (`Method.asRouter()`), so a route bound only
 * to GET responds to a HEAD request with a 405. Uptime monitors and link-preview services probe pages with HEAD,
 * so without this a healthy site reports as down. The response returned for HEAD keeps the GET handler's status
 * and every header as-is (including Content-Length, which RFC 9110 lets a HEAD response carry over from GET or
 * omit) with only the body removed. Every other method passes through untouched.
 *
 * This is a handler decorator rather than a [org.http4k.core.Filter] on purpose. `Filter.then(RoutingHttpHandler)`
 * folds the filter into each route, where it runs only after the route's method has already been matched
 * against the original request; a HEAD rewritten to GET at that point reaches the 405 responder, not the GET
 * handler. Taking the handler as a plain [HttpHandler] keeps the rewrite outside the routing decision, so
 * `HeadAsGet(routes(...))` matches the GET route. Filters that must observe the original method (an access log)
 * are composed outside it: `accessLog.then(HeadAsGet(routes))`; filters whose bodies must be dropped for HEAD
 * (error pages) go inside: `HeadAsGet(ErrorPages(...).then(routes))`.
 */
object HeadAsGet {

    operator fun invoke(next: HttpHandler): HttpHandler = { request ->
        if (request.method == Method.HEAD) {
            next(request.method(Method.GET)).body(Body.EMPTY)
        } else {
            next(request)
        }
    }
}
