package invirt.core

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

@Suppress("ktlint:standard:function-naming")
infix fun String.GET(handler: (Request) -> Response): RoutingHttpHandler = this bind Method.GET to { request ->
    handler(request)
}

@Suppress("ktlint:standard:function-naming")
infix fun String.POST(handler: (Request) -> Response): RoutingHttpHandler = this bind Method.POST to { request ->
    handler(request)
}

@Suppress("ktlint:standard:function-naming")
infix fun String.PUT(handler: (Request) -> Response): RoutingHttpHandler = this bind Method.PUT to { request ->
    handler(request)
}

@Suppress("ktlint:standard:function-naming")
infix fun String.DELETE(handler: (Request) -> Response): RoutingHttpHandler = this bind Method.DELETE to { request ->
    handler(request)
}
