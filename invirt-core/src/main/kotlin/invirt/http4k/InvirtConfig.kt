package invirt.http4k

import invirt.http4k.config.developmentMode
import invirt.http4k.views.InvirtPebbleTemplates
import invirt.pebble.InvirtPebbleExtension
import invirt.utils.threads.withValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.extension.Extension
import io.validk.ValidationErrors
import org.http4k.config.Environment
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.RequestContextKey
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

private val log = KotlinLogging.logger {}

data class InvirtConfig(
    val developmentMode: Boolean = Environment.ENV.developmentMode,
    val staticAssetsVersion: String? = null,
    val pebble: InvirtPebbleConfig = InvirtPebbleConfig()
)

data class InvirtPebbleConfig(
    val classpathLocation: String = "webapp/views",
    val hotReloadDirectory: String = "src/main/resources/webapp/views",
    val extensions: List<Extension> = emptyList(),
    val globalVariables: Map<String, Any> = emptyMap()
)

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
            Body.viewModel(pebbleTemplates.HotReload(config.pebble.hotReloadDirectory), TEXT_HTML).toLens()
        } else {
            log.info { "Loading views from classpath ${config.pebble.classpathLocation}" }
            Body.viewModel(pebbleTemplates.CachingClasspath(config.pebble.classpathLocation), TEXT_HTML).toLens()
        }

        return InvirtRequestContext.filter()
    }
}

object InvirtRequestContext {
    val http4kRequestContexts = RequestContexts()
    private val requestThreadLocal = ThreadLocal<Request>()
    private val validationErrorContextKey = RequestContextKey.optional<ValidationErrors>(http4kRequestContexts)

    internal fun filter(): Filter {
        val storeRequestOnCurrentThread = Filter { next ->
            { request ->
                requestThreadLocal.withValue(InvirtRequest(request)) {
                    next(request)
                }
            }
        }
        return ServerFilters.InitialiseRequestContext(http4kRequestContexts)
            .then(storeRequestOnCurrentThread)
    }

    val request: Request? get() = requestThreadLocal.get()

    internal fun setErrors(errors: ValidationErrors) {
        validationErrorContextKey[requestThreadLocal.get()] = errors
    }

    val errors: ValidationErrors? get() = requestThreadLocal.get()?.let { validationErrorContextKey[it] }
}
