package examples.quickstart

import invirt.http4k.GET
import invirt.http4k.views.ViewResponse
import invirt.http4k.views.initialiseInvirtViews
import invirt.http4k.views.ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class IndexResponse(val currentUsername: String) : ViewResponse("index")

class Application {

    fun start() {
        initialiseInvirtViews()

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
