package examples.quickstart

import invirt.core.GET
import invirt.core.views.InvirtView
import invirt.core.views.ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class IndexResponse(val currentUsername: String) : InvirtView("index")

class Application {

    fun start() {
        val appHandler = routes(
            "/" GET { request ->
                IndexResponse(currentUsername = "email@test.com").ok(request)
            }
        )

        val server = Netty(8080).toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port()}" }
    }
}

fun main() {
    Application().start()
}
