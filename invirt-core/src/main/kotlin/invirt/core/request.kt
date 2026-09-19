package invirt.core

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.http4k.routing.RoutedMessage

/**
 * The value of the `{[name]}` path variable of the route this request matched, or null when there is none
 * to read.
 *
 * Null covers every way the answer can be absent, which is what makes this usable from a filter wrapping a
 * whole route group: http4k attaches the uri template outside the group's filters but only on the branch
 * that matched a route, so the same filter also sees the requests that matched no route at all (a 404) or
 * matched the path with the wrong method (a 405), and those carry no template. http4k's own
 * `Request.path(name)` throws on them, which would turn a 404 into a 500. A blank value is null too - a
 * path variable that matched nothing is nothing.
 */
fun Request.pathOrNull(name: String): String? {
    val uriTemplate = (this as? RoutedMessage)?.xUriTemplate ?: return null
    if (!uriTemplate.matches(uri.path)) {
        return null
    }
    return uriTemplate.extract(uri.path)[name]?.takeIf { it.isNotBlank() }
}

/**
 * This request's path with its query string appended when it has one, e.g. `/search?q=blue` - the part of
 * the URL to hand back to a browser as a relative destination.
 */
fun Request.pathWithQuery(): String = if (uri.query.isEmpty()) {
    uri.path
} else {
    "${uri.path}?${uri.query}"
}

/**
 * Creates a request with cookies from the given [Response].
 */
fun Request.cookiesFrom(response: Response): Request = withCookies(response.cookies())

/**
 * Adds the given list of [Cookie]s to the request.
 */
fun Request.withCookies(cookies: List<Cookie>): Request {
    var request = this
    cookies.forEach {
        request = request.cookie(it)
    }
    return request
}
