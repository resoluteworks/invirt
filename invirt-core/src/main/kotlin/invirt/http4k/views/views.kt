package invirt.http4k.views

import invirt.pebble.InvirtPebbleExtension
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.extension.Extension
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

internal lateinit var defaultViewLens: BiDiBodyLens<ViewModel>
private val log = KotlinLogging.logger {}

/**
 * Initialises the default Invirt view lens to render Pebble templates.
 *
 * @param hotReload whether to use hot reload for templates
 * @param staticAssetsVersion the version of static assets to use in the templates
 * @param classpathLocation the location of the templates on the classpath
 * @param hotReloadDirectory the directory to use for hot reload
 * @param pebbleExtensions additional Pebble extensions to use
 */
fun initialiseInvirtViews(
    hotReload: Boolean = false,
    staticAssetsVersion: String? = null,
    classpathLocation: String = "webapp/views",
    hotReloadDirectory: String = "src/main/resources/webapp/views",
    pebbleExtensions: List<Extension> = emptyList()
) {
    val pebbleTemplates = pebbleTemplates(staticAssetsVersion, pebbleExtensions)

    defaultViewLens = if (hotReload) {
        log.info { "Loading views from hot reload directory $hotReloadDirectory" }
        Body.viewModel(pebbleTemplates.HotReload(hotReloadDirectory), TEXT_HTML).toLens()
    } else {
        log.info { "Loading views from classpath $classpathLocation" }
        Body.viewModel(pebbleTemplates.CachingClasspath(classpathLocation), TEXT_HTML).toLens()
    }
}

internal fun pebbleTemplates(staticAssetsVersion: String? = null, extensions: List<Extension>): InvirtPebbleTemplates =
    InvirtPebbleTemplates(configure = { builder ->
        extensions.forEach { builder.extension(it) }
        builder.extension(
            InvirtPebbleExtension(
                staticAssetsVersion = staticAssetsVersion
            )
        )
        builder
    })

/**
 * Renders the specified template with the given model object.
 */
fun renderTemplate(template: String): Response = Response(Status.OK).with(
    defaultViewLens of object : ViewModel {
        override fun template() = template
    }
)

/**
 * A response that renders a view (template) from a model object and a status OK.
 */
fun ViewModel.ok(): Response = this.status(Status.OK)

/**
 * A response that renders a view (template) from a model object and a status.
 */
fun ViewModel.status(status: Status): Response = Response(status).with(defaultViewLens of this)

/**
 * Convenience class for a [ViewModel] that renders a view (template).
 */
open class ViewResponse(private val template: String) : ViewModel {
    override fun template() = template
}
