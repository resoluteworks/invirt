package examples.authentication

import examples.authentication.handlers.DashboardHandler
import examples.authentication.handlers.IndexHandler
import examples.authentication.handlers.LoginHandler
import invirt.http4k.InvirtFilter
import invirt.http4k.config.developmentMode
import invirt.http4k.security.authentication.AuthenticationFilter
import invirt.http4k.security.authentication.authenticatedRoutes
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

        val authService = AuthenticationService()
        val appHandler = InvirtFilter()
            .then(AuthenticationFilter(authService))
            .then(
                routes(
                    IndexHandler(),
                    LoginHandler(authService),

                    // These routes require a valid Principal to be accessed
                    authenticatedRoutes(
                        DashboardHandler()
                    )
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
