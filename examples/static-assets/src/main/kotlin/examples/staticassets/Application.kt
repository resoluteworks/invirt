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
        val staticAssetsVersion = gitCommitId()!!
        val devMode = Environment.ENV.developmentMode

        val config = InvirtConfig(
            developmentMode = devMode,
            pebble = InvirtPebbleConfig(
                globalVariables = mapOf("staticAssetsVersion" to staticAssetsVersion)
            )
        )
        val appHandler = Invirt(config).then(
            routes(
                "/" GET { renderTemplate("index") },
                "/static/${staticAssetsVersion}" bind cacheDays(365).then(staticAssets(devMode))
            )
        )

        val server = Netty(8080).toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port()}" }
    }
}

fun main() {
    Application().start()
}
