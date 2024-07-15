package invirt.http4k.handlers

import invirt.http4k.cacheDays
import invirt.http4k.handlers.StaticResources.Classpath
import invirt.http4k.handlers.StaticResources.HotReload
import org.http4k.core.then
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static

/**
 * An http4k handler for serving static resources from classpath or a directory using an assets version.
 *
 * The [Classpath] version binds `/static` to `classpath:/webapp/static`.
 *
 * The [HotReload] version binds `/static` to the `src/main/resources/webapp/static` directory, which
 * can be used in development for hot reload/refresh.
 *
 * Both versions bind `/static/assets/${version}` to `classpath:/webapp/static/assets` and
 * `src/main/resources/webapp/static/assets` respectively, to allow for resource versioning.
 *
 * Invoking the object directly will create either a [Classpath] or a [HotReload] handler, depending
 * on the value of the `hotReload` boolean.
 *
 * Resource responses are set to be cached by default for 1 year (365 days)
 */
@Suppress("ktlint:standard:function-naming")
object StaticResources {

    operator fun invoke(hotReload: Boolean, version: String): RoutingHttpHandler = if (hotReload) HotReload(version) else Classpath(version)

    fun Classpath(
        version: String,
        classpathDir: String = "webapp/static",
        cacheDays: Int = 365
    ): RoutingHttpHandler = "/static" bind cacheDays(cacheDays).then(
        routes(
            "/assets/${version}" bind static(ResourceLoader.Classpath("$classpathDir/assets")),
            "/" bind static(ResourceLoader.Classpath(classpathDir))
        )
    )

    fun HotReload(
        version: String,
        directory: String = "src/main/resources/webapp/static",
        cacheDays: Int = 365
    ): RoutingHttpHandler = "/static" bind cacheDays(cacheDays).then(
        routes(
            "/assets/${version}" bind static(ResourceLoader.Directory("$directory/assets")),
            "/" bind static(ResourceLoader.Directory(directory))
        )
    )
}
