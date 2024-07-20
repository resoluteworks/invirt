package examples.data

import examples.data.handlers.OrderHandler
import examples.data.service.OrderService
import invirt.http4k.InvirtFilter
import invirt.http4k.config.developmentMode
import invirt.http4k.views.initialiseInvirtViews
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.cloudnative.env.Environment
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class Application {

    fun start() {
        val devMode = Environment.ENV.developmentMode
        initialiseInvirtViews(hotReload = devMode)

        val orderService = OrderService()
        val appHandler = InvirtFilter().then(
            routes(
                OrderHandler(orderService)
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
