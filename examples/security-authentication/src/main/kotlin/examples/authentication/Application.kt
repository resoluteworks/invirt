package examples.authentication

import examples.authentication.handlers.DashboardHandler
import examples.authentication.handlers.IndexHandler
import examples.authentication.handlers.LoginHandler
import examples.authentication.service.AuthenticationService
import invirt.http4k.InvirtFilter
import invirt.http4k.config.developmentMode
import invirt.http4k.security.authentication.AuthenticationFilter
import invirt.http4k.security.authentication.Principal
import invirt.http4k.security.authentication.authenticatedRoutes
import invirt.http4k.views.initialiseInvirtViews
import invirt.pebble.functions.pebbleFunction
import invirt.pebble.pebbleFunctions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.cloudnative.env.Environment
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class Application {

    fun start() {
        val devMode = Environment.ENV.developmentMode

        // A custom Pebble extension with nothing else than a currentUser() function
        // to display this information on the dashboard
        val pebbleExtensions = listOf(
            pebbleFunctions(
                pebbleFunction("currentUser") { Principal.current }
            )
        )

        initialiseInvirtViews(hotReload = devMode, pebbleExtensions = pebbleExtensions)

        val authService = AuthenticationService()

        val appHandler = InvirtFilter()
            .then(AuthenticationFilter(authService))
            .then(
                routes(
                    // These routes are public and anyone can access them
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
