package invirt.core.handlers

import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.static

/**
 * Create a handler that serves static assets from the classpath or a directory.
 * If hotReload is true, the directory is used, otherwise the classpath is used.
 * The default classpath location is "webapp/static" and the default directory is "src/main/resources/webapp/static".
 *
 * @param developmentMode whether to use the directory for static assets or the classpath.
 * @param classpathLocation the classpath location to serve static assets from.
 * @param directory the directory to serve static assets from.
 */
fun staticAssets(
    developmentMode: Boolean,
    classpathLocation: String = "webapp/static",
    directory: String = "src/main/resources/webapp/static"
): RoutingHttpHandler = if (developmentMode) {
    static(ResourceLoader.Directory(directory))
} else {
    static(ResourceLoader.Classpath(classpathLocation))
}
