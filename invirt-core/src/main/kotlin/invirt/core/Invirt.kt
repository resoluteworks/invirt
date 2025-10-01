package invirt.core

import invirt.core.views.InvirtPebbleTemplates
import invirt.core.views.InvirtRenderModel
import invirt.pebble.InvirtPebbleExtension
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.string

private val log = KotlinLogging.logger {}

object Invirt {

    private lateinit var defaultViewLens: BiDiBodyLens<InvirtRenderModel>
    internal val viewLens: BiDiBodyLens<InvirtRenderModel> get() = defaultViewLens

    operator fun invoke(config: InvirtConfig = InvirtConfig()): Filter {
        val pebbleTemplates = InvirtPebbleTemplates(configure = { builder ->
            config.pebble.extensions.forEach { builder.extension(it) }
            builder.extension(InvirtPebbleExtension(config.pebble.globalVariables))
            builder.greedyMatchMethod(true)
            builder
        })

        defaultViewLens = if (config.developmentMode) {
            log.info { "Loading Invirt views from hot reload directory ${config.pebble.hotReloadDirectory}" }
            Body.invirtViewModel(pebbleTemplates.hotReload(config.pebble.hotReloadDirectory), ContentType.TEXT_HTML).toLens()
        } else {
            log.info { "Loading Invirt views from classpath ${config.pebble.classpathLocation}" }
            Body.invirtViewModel(pebbleTemplates.cachingClasspath(config.pebble.classpathLocation), ContentType.TEXT_HTML).toLens()
        }

        return Filter { next ->
            { req -> next(req) }
        }
    }
}

internal typealias InvirtTemplateRenderer = (InvirtRenderModel) -> String

internal fun Body.Companion.invirtViewModel(renderer: InvirtTemplateRenderer, contentType: ContentType) =
    string(contentType)
        .map<InvirtRenderModel>({ throw UnsupportedOperationException("Cannot parse a ViewModel") }, renderer::invoke)
