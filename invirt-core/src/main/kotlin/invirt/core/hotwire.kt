package invirt.core

import org.http4k.core.Response
import org.http4k.core.Status

/**
 * A response that renders a view as a Turbo Stream.
 */
fun Response.turboStream(): Response = replaceHeader("Content-Type", "text/vnd.turbo-stream.html")

/**
 * A response that renders a Turbo Stream action to refresh the page.
 */
fun turboStreamRefresh(): Response = Response(Status.OK)
    .body("""<turbo-stream action="refresh"></turbo-stream>""")
    .turboStream()

/**
 * A response that renders a Turbo Stream action to navigate the browser to [url].
 *
 * Turbo swaps the body of a stream response into the current page rather than following a redirect, so a
 * handler answering a Turbo form submission sends this instead of a `303` when the next thing the user
 * should see is another page.
 */
fun turboStreamRedirect(url: String): Response = Response(Status.OK)
    .body("""<turbo-stream action="redirect" target="$url"></turbo-stream>""")
    .turboStream()
