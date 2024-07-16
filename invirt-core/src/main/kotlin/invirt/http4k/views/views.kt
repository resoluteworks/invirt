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

private lateinit var defaultViewLens: BiDiBodyLens<ViewModel>
private val log = KotlinLogging.logger {}

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

private fun pebbleTemplates(staticAssetsVersion: String? = null, extensions: List<Extension>): InvirtPebbleTemplates =
    InvirtPebbleTemplates(configure = { builder ->
        extensions.forEach { builder.extension(it) }
        builder.extension(
            InvirtPebbleExtension(
                staticAssetsVersion = staticAssetsVersion
            )
        )
        builder
    })

fun renderTemplate(template: String): Response = Response(Status.OK).with(
    defaultViewLens of object : ViewModel {
        override fun template() = template
    }
)

fun ViewModel.ok(): Response = this.status(Status.OK)

fun ViewModel.status(status: Status): Response = Response(status).with(defaultViewLens of this)

open class ViewResponse(private val template: String) : ViewModel {
    override fun template() = template
}
