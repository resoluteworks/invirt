package invirt.core

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import kotlin.collections.forEach

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
