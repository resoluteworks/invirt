package invirt.http4k.handlers

import invirt.http4k.cacheDays
import org.http4k.core.then
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static

/**
 * Binds
 *  - /static to resources/webapp/static
 *  - /static/assets/${version} to resources/webapp/static/assets, to allow versioning of CSS/JS/etc.
 *
 *  Static resources will be cached by default for 1 year (365 days)
 */
@Suppress("ktlint:standard:function-naming")
object StaticResources {

    operator fun invoke(hotReload: Boolean, version: String): RoutingHttpHandler = if (hotReload) HotReload(version) else Classpath(version)

    fun Classpath(
        version: String,
        classpathDir: String = "webapp/static",
        cacheDays: Int = 365
    ): RoutingHttpHandler = "/static" bind routes(
        "/assets/${version}" bind cacheDays(cacheDays)
            .then(static(ResourceLoader.Classpath("$classpathDir/assets"))),

        "/" bind static(ResourceLoader.Classpath(classpathDir))
    )

    fun HotReload(
        version: String,
        directory: String = "src/main/resources/webapp/static",
        cacheDays: Int = 365
    ): RoutingHttpHandler = "/static" bind routes(
        "/assets/${version}" bind cacheDays(cacheDays)
            .then(static(ResourceLoader.Directory("$directory/assets"))),

        "/" bind static(ResourceLoader.Directory(directory))
    )
}
