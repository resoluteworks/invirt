package invirt.core.views

import invirt.core.InvirtRequestContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.loader.FileLoader
import org.http4k.template.TemplateRenderer
import org.http4k.template.Templates
import org.http4k.template.ViewModel
import org.http4k.template.ViewNotFound
import java.io.StringWriter

private val log = KotlinLogging.logger {}

/**
 * A [Templates] implementation that uses Pebble as the template engine.
 */
class InvirtPebbleTemplates(
    private val configure: (PebbleEngine.Builder) -> PebbleEngine.Builder = { it },
    private val classLoader: ClassLoader = ClassLoader.getSystemClassLoader()
) : Templates {

    private class PebbleTemplateRenderer(private val engine: PebbleEngine) : TemplateRenderer {
        override fun invoke(viewModel: ViewModel): String {
            var template = viewModel.template()
            if (!template.endsWith(".peb")) {
                template += ".peb"
            }

            return try {
                val writer = StringWriter()

                val context = if (viewModel is ErrorResponseView) {
                    InvirtRequestContext.setErrors(viewModel.errors)
                    mapOf(
                        "model" to viewModel.model, "errors" to viewModel.errors
                    )
                } else {
                    mapOf("model" to viewModel)
                }

                engine.getTemplate(template).evaluate(
                    writer, context.plus("request" to InvirtRequestContext.request)
                )
                writer.toString()
            } catch (e: LoaderException) {
                log.error(e) { "Error loading template $template: ${e.message}" }
                throw ViewNotFound(viewModel)
            }
        }
    }

    override fun CachingClasspath(baseClasspathPackage: String): TemplateRenderer {
        val loader = ClasspathLoader(classLoader)
        loader.prefix = if (baseClasspathPackage.isEmpty()) null else baseClasspathPackage.replace('.', '/')
        return PebbleTemplateRenderer(configure(PebbleEngine.Builder().loader(loader)).build())
    }

    override fun Caching(baseTemplateDir: String): TemplateRenderer =
        throw UnsupportedOperationException("Invirt doesn't support templates cached on disk")

    override fun HotReload(baseTemplateDir: String): TemplateRenderer {
        val loader = FileLoader()
        loader.prefix = baseTemplateDir
        return PebbleTemplateRenderer(configure(PebbleEngine.Builder().cacheActive(false).loader(loader)).build())
    }
}
