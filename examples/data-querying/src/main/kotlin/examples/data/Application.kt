package examples.data

import examples.data.handlers.OrderHandler
import examples.data.service.OrderService
import invirt.core.Invirt
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class Application {

    fun start() {
        val orderService = OrderService()
        val appHandler = Invirt().then(
            routes(
                OrderHandler(orderService)
            )
        )

        val server = Netty(8080).toServer(appHandler)
        server.start()
        log.info { "Server started at http://localhost:${server.port()}" }
    }
}

fun main() {
    Application().start()
}
