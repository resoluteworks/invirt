package examples.staticassets

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtConfig
import invirt.core.InvirtPebbleConfig
import invirt.core.cacheDays
import invirt.core.config.developmentMode
import invirt.core.config.gitCommitId
import invirt.core.handlers.staticAssets
import invirt.core.views.renderTemplate
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

        val config = InvirtConfig(
            pebble = InvirtPebbleConfig(
                globalVariables = mapOf("staticAssetsVersion" to assetsVersion)
            )
        )
        val appHandler = Invirt(config).then(
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
