package invirt.core

import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.cookie.invalidate
import org.http4k.core.cookie.replaceCookie
import org.http4k.core.with
import org.http4k.lens.Header

/**
 * Returns an updated response with the specified cookies.
 */
fun Response.withCookies(cookies: Collection<Cookie>): Response {
    var response = this
    cookies.forEach {
        response = response.cookie(it)
    }
    return response
}

/**
 * Returns an updated response with the specified cookies, ignoring any whose name the response
 * already carries a `Set-Cookie` header for.
 *
 * [withCookies] appends a `Set-Cookie` header per cookie, so setting a cookie the response already
 * has ships both headers and the browser keeps the last one. Use this variant when the cookies come
 * from something that ran before the response was built - an authentication filter refreshing a
 * session token, say - so that what the handler decided wins. The case that matters is a sign-out
 * handler invalidating a session cookie: a refreshed cookie appended after it would silently
 * re-authenticate the user.
 *
 * Cookies are matched on name alone. RFC 6265 keys a browser's cookie store on name, domain and
 * path, but a caller's scope is not knowable here - an absent path means "the directory of the
 * request URI", not "/", so a `null` path and an explicit `/` cannot be told apart without the
 * request. http4k's own [replaceCookie] is name-keyed for the same reason. Name matching is also the
 * safe direction of error: at worst it drops a refresh, it can never resurrect a cookie the handler
 * cleared.
 */
fun Response.withCookiesIfAbsent(cookies: Collection<Cookie>): Response {
    val existingNames = this.cookies().map { it.name }.toSet()
    return withCookies(cookies.filterNot { it.name in existingNames })
}

/**
 * Returns an updated response with the specified cookies invalidated.
 */
fun Response.invalidateCookies(cookies: Collection<Cookie>): Response {
    var response = this
    cookies.forEach {
        response = response.replaceCookie(it.invalidate())
    }
    return response
}

/**
 * Returns a 303 redirect response with the specified location.
 */
fun httpSeeOther(location: String): Response = Response(Status.SEE_OTHER)
    .with(Header.LOCATION of Uri.of(location))

/**
 * Returns a 404 not found response.
 */
fun httpNotFound(): Response = Response(Status.NOT_FOUND)

/**
 * When an HTTP 200 is required before a redirect, for example
 * https://stackoverflow.com/questions/42216700/how-can-i-redirect-after-oauth2-with-samesite-strict-and-still-get-my-cookies
 */
fun htmlRedirect(url: String): Response {
    val body = """<html><head><meta http-equiv="refresh" content="0;URL='${url}'"/></head></html>"""
    return Response(Status.OK).body(body)
}
