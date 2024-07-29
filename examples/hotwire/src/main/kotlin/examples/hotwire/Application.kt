package examples.hotwire

import invirt.http4k.GET
import invirt.http4k.InvirtFilter
import invirt.http4k.config.developmentMode
import invirt.http4k.views.initialiseInvirtViews
import invirt.http4k.views.renderTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.config.Environment
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class Application {

    fun start() {
        initialiseInvirtViews(hotReload = Environment.ENV.developmentMode)

        val appHandler = InvirtFilter()
            .then(
                routes(
                    "/" GET { renderTemplate("send-parcel") }
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
