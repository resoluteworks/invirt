package invirt.core.views

import invirt.core.InvirtException
import invirt.core.InvirtPebbleConfig
import invirt.core.InvirtRequest
import invirt.core.InvirtTemplateRenderer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.loader.FileLoader
import java.io.File
import java.io.StringWriter

private val log = KotlinLogging.logger {}

internal fun cachingClasspathTemplates(
    pebbleConfig: InvirtPebbleConfig,
    baseClasspathPackage: String,
    configure: (PebbleEngine.Builder) -> PebbleEngine.Builder = { it }
): InvirtTemplateRenderer {
    val loader = ClasspathLoader(ClassLoader.getSystemClassLoader())
    loader.prefix = if (baseClasspathPackage.isEmpty()) null else baseClasspathPackage.replace('.', '/')
    return PebbleTemplateRenderer(pebbleConfig, configure(PebbleEngine.Builder().loader(loader)).build())
}

internal fun hotReloadTemplates(
    pebbleConfig: InvirtPebbleConfig,
    baseTemplateDir: String,
    configure: (PebbleEngine.Builder) -> PebbleEngine.Builder = { it }
): InvirtTemplateRenderer {
    val loader = FileLoader(File(baseTemplateDir).absolutePath)
    return PebbleTemplateRenderer(pebbleConfig, configure(PebbleEngine.Builder().cacheActive(false).loader(loader)).build())
}

private class PebbleTemplateRenderer(
    private val pebbleConfig: InvirtPebbleConfig,
    private val engine: PebbleEngine
) : InvirtTemplateRenderer {
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

            pebbleConfig.contextVariables.forEach { (name, function) ->
                context[name] = function(invirtRenderModel.request)
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
