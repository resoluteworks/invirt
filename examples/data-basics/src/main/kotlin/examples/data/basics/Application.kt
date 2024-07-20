package examples.data.basics

import examples.data.basics.handlers.OrderHandler
import examples.data.basics.repository.OrderRepository
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

        val orderRepository = OrderRepository()
        val appHandler = InvirtFilter().then(
            routes(
                OrderHandler(orderRepository)
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
