package invirt.core.filters

import invirt.core.views.renderTemplate
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * An http4k filter that renders a view (template) for a given status code.
 */
object ErrorPages {

    operator fun invoke(vararg statusViewMappings: Pair<Status, String>): Filter = invoke(statusViewMappings.toMap())

    operator fun invoke(statusViewMappings: Map<Status, String>): Filter = Filter { next ->
        { request ->
            val response = next(request)
            val view = statusViewMappings[response.status]
            if (view != null) {
                renderTemplate(request, view)
                    .status(response.status)
                    .withSetCookiesFrom(response)
            } else {
                response
            }
        }
    }
}

/**
 * Copies the `Set-Cookie` headers of [original] onto this response, verbatim.
 *
 * The error page is a freshly rendered response, so everything the handler put on its own response is
 * otherwise lost. Cookies have to survive that: a handler that invalidated the session cookie and returned
 * a status which renders as an error page still signed the user out, and dropping its `Set-Cookie` would
 * silently leave them signed in. Only cookies are carried - the rendered body has its own `Content-Type`
 * and `Content-Length`, and the handler's remaining headers describe a body that no longer exists.
 */
private fun Response.withSetCookiesFrom(original: Response): Response =
    original.headerValues("Set-Cookie")
        .filterNotNull()
        .fold(this) { response, setCookie -> response.header("Set-Cookie", setCookie) }
