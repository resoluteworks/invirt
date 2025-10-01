package invirt.core.views

import invirt.core.InvirtException
import invirt.core.InvirtRequest
import invirt.core.InvirtTemplateRenderer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.loader.FileLoader
import org.http4k.template.Templates
import java.io.StringWriter

private val log = KotlinLogging.logger {}

/**
 * A [Templates] implementation that uses Pebble as the template engine.
 */
internal class InvirtPebbleTemplates(
    private val configure: (PebbleEngine.Builder) -> PebbleEngine.Builder = { it },
    private val classLoader: ClassLoader = ClassLoader.getSystemClassLoader()
) {

    private class PebbleTemplateRenderer(private val engine: PebbleEngine) : InvirtTemplateRenderer {
        override fun invoke(invirtRenderModel: InvirtRenderModel): String {
            var template = invirtRenderModel.template
            if (!template.endsWith(".peb")) {
                template += ".peb"
            }

            return try {
                val writer = StringWriter()
                val context = mutableMapOf<String, Any?>()
                context["request"] = InvirtRequest(invirtRenderModel.request)
                context["model"] = invirtRenderModel.model
                if (invirtRenderModel.errors != null) {
                    context["errors"] = invirtRenderModel.errors
                }

                engine.getTemplate(template).evaluate(writer, context)
                writer.toString()
            } catch (e: LoaderException) {
                val message = "Error loading template $template: ${e.message}"
                log.error(e) { message }
                throw InvirtException(message, e)
            }
        }
    }

    internal fun cachingClasspath(baseClasspathPackage: String): InvirtTemplateRenderer {
        val loader = ClasspathLoader(classLoader)
        loader.prefix = if (baseClasspathPackage.isEmpty()) null else baseClasspathPackage.replace('.', '/')
        return PebbleTemplateRenderer(configure(PebbleEngine.Builder().loader(loader)).build())
    }

    internal fun hotReload(baseTemplateDir: String): InvirtTemplateRenderer {
        val loader = FileLoader()
        loader.prefix = baseTemplateDir
        return PebbleTemplateRenderer(configure(PebbleEngine.Builder().cacheActive(false).loader(loader)).build())
    }
}
