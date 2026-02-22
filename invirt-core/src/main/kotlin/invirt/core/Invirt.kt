package invirt.core

import invirt.core.config.developmentMode
import invirt.core.views.InvirtRenderModel
import invirt.core.views.cachingClasspathTemplates
import invirt.core.views.hotReloadTemplates
import invirt.pebble.InvirtPebbleExtension
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import org.http4k.config.Environment
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.string

private val log = KotlinLogging.logger {}

object Invirt {

    internal lateinit var viewLens: BiDiBodyLens<InvirtRenderModel>

    init {
        configure()
    }

    fun configure(
        developmentMode: Boolean = Environment.ENV.developmentMode,
        pebble: InvirtPebbleConfig = InvirtPebbleConfig()
    ) {
        val configure: (PebbleEngine.Builder) -> PebbleEngine.Builder = { builder ->
            pebble.extensions.forEach { builder.extension(it) }
            builder.extension(InvirtPebbleExtension(pebble.globalVariables))
            builder.greedyMatchMethod(true)
            builder
        }

        viewLens = if (developmentMode) {
            log.info { "Loading Invirt views from hot reload directory ${pebble.hotReloadDirectory}" }
            Body.invirtViewModel(hotReloadTemplates(pebble, pebble.hotReloadDirectory, configure), ContentType.TEXT_HTML)
                .toLens()
        } else {
            log.info { "Loading Invirt views from classpath ${pebble.classpathLocation}" }
            Body.invirtViewModel(
                cachingClasspathTemplates(pebble, pebble.classpathLocation, configure),
                ContentType.TEXT_HTML
            ).toLens()
        }
    }
}

internal typealias InvirtTemplateRenderer = (InvirtRenderModel) -> String

internal fun Body.Companion.invirtViewModel(renderer: InvirtTemplateRenderer, contentType: ContentType) =
    string(contentType).map<InvirtRenderModel>({ throw UnsupportedOperationException("Cannot parse a ViewModel") }, renderer::invoke)
