package invirt.core

import org.http4k.core.Response
import org.http4k.core.Status

/**
 * A response that renders a view as a Turbo Stream.
 */
fun Response.turboStream(): Response = header("Content-Type", "text/vnd.turbo-stream.html")

/**
 * A response that renders a Turbo Stream action to refresh the page.
 */
fun turboStreamRefresh(): Response = Response(Status.OK)
    .body("""<turbo-stream action="refresh"></turbo-stream>""")
    .turboStream()
