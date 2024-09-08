package invirt.core

import invirt.core.views.InvirtPebbleTemplates
import invirt.pebble.InvirtPebbleExtension
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.lens.BiDiBodyLens
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

private val log = KotlinLogging.logger {}

object Invirt {
    private lateinit var defaultViewLens: BiDiBodyLens<ViewModel>
    val viewLens: BiDiBodyLens<ViewModel> get() = defaultViewLens

    operator fun invoke(config: InvirtConfig = InvirtConfig()): Filter {
        val pebbleTemplates = InvirtPebbleTemplates(configure = { builder ->
            config.pebble.extensions.forEach { builder.extension(it) }
            builder.extension(InvirtPebbleExtension(config.pebble.globalVariables))
            builder
        })

        defaultViewLens = if (config.developmentMode) {
            log.info { "Loading views from hot reload directory ${config.pebble.hotReloadDirectory}" }
            Body.viewModel(pebbleTemplates.HotReload(config.pebble.hotReloadDirectory), ContentType.TEXT_HTML).toLens()
        } else {
            log.info { "Loading views from classpath ${config.pebble.classpathLocation}" }
            Body.viewModel(pebbleTemplates.CachingClasspath(config.pebble.classpathLocation), ContentType.TEXT_HTML).toLens()
        }

        return InvirtRequestContext.filter()
    }
}
