package examples.quickstart

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.config.developmentMode
import invirt.core.views.InvirtView
import invirt.core.views.ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.config.Environment
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class HomepageResponse(val currentUsername: String) : InvirtView("index")

class Application {

    fun start() {
        Invirt.configure(
            developmentMode = Environment.ENV.developmentMode
        )

        val appHandler = routes(
            "/" GET { request ->
                HomepageResponse(currentUsername = "email@test.com").ok(request)
            }
        )

        val server = Netty(8080).toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port()}" }
    }
}

fun main() {
    Application().start()
}
