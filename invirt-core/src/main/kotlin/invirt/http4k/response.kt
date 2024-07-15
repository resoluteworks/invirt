package invirt.http4k

import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.replaceCookie
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Header
import java.time.Instant

fun <T : Any> BiDiBodyLens<T>.ok(message: T): Response = Response(Status.OK).with(this of message)

fun Response.withCookies(cookies: Collection<Cookie>): Response {
    var response = this
    cookies.forEach {
        response = response.cookie(it)
    }
    return response
}

fun Response.invalidateCookies(cookies: Collection<Cookie>): Response {
    var response = this
    cookies.forEach {
        response = response.replaceCookie(it.copy(value = "deleted", expires = Instant.EPOCH))
    }
    return response
}

fun httpSeeOther(location: String): Response {
    return Response(Status.SEE_OTHER)
        .with(Header.LOCATION of Uri.of(location))
}

fun httpNotFound(): Response {
    return Response(Status.NOT_FOUND)
}

/**
 * When an HTTP 200 is required before a redirect, for example https://stackoverflow.com/questions/42216700/how-can-i-redirect-after-oauth2-with-samesite-strict-and-still-get-my-cookies
 */
fun htmlRedirect(url: String): Response {
    val body = """<html><head><meta http-equiv="refresh" content="0;URL='${url}'"/></head></html>"""
    return Response(Status.OK).body(body)
}
