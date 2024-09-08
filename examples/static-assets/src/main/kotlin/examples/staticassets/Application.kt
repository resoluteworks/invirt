package examples.staticassets

import invirt.http4k.GET
import invirt.http4k.cacheDays
import invirt.http4k.config.developmentMode
import invirt.http4k.config.gitCommitId
import invirt.http4k.handlers.staticAssets
import invirt.http4k.views.initialiseInvirtViews
import invirt.http4k.views.renderTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.config.Environment
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class Application {

    fun start() {
        val assetsVersion = gitCommitId()!!
        val developmentMode = Environment.ENV.developmentMode

        initialiseInvirtViews(
            hotReload = developmentMode,
            staticAssetsVersion = assetsVersion
        )

        val appHandler = Invirt().then(
            routes(
                "/" GET { renderTemplate("index") },
                "/static/${assetsVersion}" bind cacheDays(365).then(staticAssets(developmentMode))
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
