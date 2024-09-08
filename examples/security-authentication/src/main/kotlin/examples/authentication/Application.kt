package examples.authentication

import examples.authentication.handlers.DashboardHandler
import examples.authentication.handlers.IndexHandler
import examples.authentication.handlers.LoginHandler
import examples.authentication.handlers.LogoutHandler
import examples.authentication.service.AuthenticationService
import invirt.core.Invirt
import invirt.core.InvirtConfig
import invirt.core.InvirtPebbleConfig
import invirt.core.filters.ErrorPages
import invirt.core.filters.StatusOverride
import invirt.pebble.functions.pebbleFunction
import invirt.pebble.pebbleFunctions
import invirt.security.authentication.AuthenticationFilter
import invirt.security.authentication.Principal
import invirt.security.authentication.authenticatedRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class Application {

    fun start() {
        // A custom Pebble extension with nothing else than a currentUser() function
        // to display this information on the dashboard
        val pebbleExtensions = listOf(
            pebbleFunctions(
                pebbleFunction("currentUser") { Principal.current }
            )
        )

        val authService = AuthenticationService()

        val appHandler = Invirt(InvirtConfig(pebble = InvirtPebbleConfig(extensions = pebbleExtensions)))
            .then(AuthenticationFilter(authService))
            .then(ErrorPages(Status.NOT_FOUND to "error/404"))
            .then(StatusOverride(Status.FORBIDDEN to Status.NOT_FOUND))
            .then(
                routes(
                    // These routes are public and anyone can access them
                    IndexHandler(),
                    LoginHandler(authService),

                    // These routes require a valid Principal to be accessed
                    authenticatedRoutes(
                        DashboardHandler(),
                        LogoutHandler(authService)
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
