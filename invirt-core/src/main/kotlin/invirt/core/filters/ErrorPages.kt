package invirt.core.filters

import invirt.core.views.renderTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status

private val log = KotlinLogging.logger {}

/**
 * An http4k filter that renders a view (template) for a given status code.
 *
 * Rendering the error page can itself fail - a context variable that reads a downed database, a template
 * that was renamed, a macro that throws - and that failure happens while the application is already
 * handling an error. [fallbackBody] is what the client gets in that case, with the status the handler
 * returned, so a raw stack trace never reaches a browser; with no fallback the response is the status and
 * an empty body. The fallback is served as `text/html; charset=utf-8` and so has to be self-contained
 * markup: a page that renders another template to produce it can fail the same way.
 *
 * Only an [Exception] is caught. An [Error] (a `StackOverflowError` from a recursive macro, an
 * `OutOfMemoryError`) escapes deliberately: the JVM is in a state where serving a page is not the useful
 * thing to do.
 */
object ErrorPages {

    operator fun invoke(vararg statusViewMappings: Pair<Status, String>, fallbackBody: String? = null): Filter =
        invoke(statusViewMappings.toMap(), fallbackBody)

    operator fun invoke(statusViewMappings: Map<Status, String>, fallbackBody: String? = null): Filter = Filter { next ->
        { request ->
            val response = next(request)
            val view = statusViewMappings[response.status]
            if (view != null) {
                try {
                    renderTemplate(request, view)
                        .status(response.status)
                        .withSetCookiesFrom(response)
                } catch (e: Exception) {
                    log.atError {
                        cause = e
                        message = "Error rendering the error page, serving the fallback body"
                        payload = mapOf("view" to view, "status" to response.status.code)
                    }
                    fallbackResponse(response.status, fallbackBody).withSetCookiesFrom(response)
                }
            } else {
                response
            }
        }
    }
}

/**
 * The response served when the error page itself fails to render: [fallbackBody] as HTML, or nothing at
 * all when no fallback was given. [status] is the status the handler returned, not a new one - the client
 * is told what happened to its request, and the failure to render the page for it is the server's problem.
 */
private fun fallbackResponse(status: Status, fallbackBody: String?): Response = if (fallbackBody != null) {
    Response(status)
        .header("Content-Type", "text/html; charset=utf-8")
        .body(fallbackBody)
} else {
    Response(status)
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
