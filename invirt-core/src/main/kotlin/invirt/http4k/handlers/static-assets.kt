package invirt.http4k.handlers

import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.static

fun staticAssets(
    hotReload: Boolean,
    classpathLocation: String = "webapp/static",
    directory: String = "src/main/resources/webapp/static"
): RoutingHttpHandler = if (hotReload) {
    static(ResourceLoader.Directory(directory))
} else {
    static(ResourceLoader.Classpath(classpathLocation))
}
