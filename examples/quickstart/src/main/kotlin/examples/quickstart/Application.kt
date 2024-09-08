package examples.quickstart

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.views.ViewResponse
import invirt.core.views.ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class IndexResponse(val currentUsername: String) : ViewResponse("index")

class Application {

    fun start() {
        val appHandler = Invirt().then(
            routes(
                "/" GET {
                    IndexResponse(currentUsername = "email@test.com").ok()
                }
            )
        )

        val server = Netty(8080)
        server.toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port}" }
    }
}

fun main() {
    Application().start()
}
