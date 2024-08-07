package invirt.http4k

import org.http4k.core.Response
import org.http4k.core.Status

fun Response.turboStream(): Response = header("Content-Type", "text/vnd.turbo-stream.html")

fun turboStreamRefresh(): Response = Response(Status.OK)
    .body("""<turbo-stream action="refresh"></turbo-stream>""")
    .turboStream()
