package invirt.core

import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
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
