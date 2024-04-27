package invirt.http4k

import invirt.http4k.views.InvirtPebbleTemplates
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

fun setDefaultViewLens(viewLens: BiDiBodyLens<ViewModel>) {
    defaultViewLens = viewLens
}

object Views {
    operator fun invoke(hotReload: Boolean = false, vararg extensions: Extension): BiDiBodyLens<ViewModel> =
        if (hotReload) {
            HotReload(extensions = extensions)
        } else {
            Classpath(extensions = extensions)
        }

    fun Classpath(classpathDir: String = "webapp/views", vararg extensions: Extension): BiDiBodyLens<ViewModel> {
        log.info { "Loading views from classpath $classpathDir" }
        return Body.viewModel(pebbleTemplates(*extensions).CachingClasspath(classpathDir), TEXT_HTML).toLens()
    }

    fun HotReload(directory: String = "src/main/resources/webapp/views", vararg extensions: Extension): BiDiBodyLens<ViewModel> {
        log.info { "Loading views from hot reload directory $directory" }
        return Body.viewModel(pebbleTemplates(*extensions).HotReload(directory), TEXT_HTML).toLens()
    }

    private fun pebbleTemplates(vararg extensions: Extension): InvirtPebbleTemplates {
        return InvirtPebbleTemplates(configure = { builder ->
            extensions.forEach { builder.extension(it) }
            builder.extension(InvirtPebbleExtension())
            builder
        })
    }
}

fun renderTemplate(
    template: String,
    viewLens: BiDiBodyLens<ViewModel> = defaultViewLens,
): Response {
    return Response(Status.OK).with(
        viewLens of
            object : ViewModel {
                override fun template() = template
            },
    )
}

fun ViewModel.ok(viewLens: BiDiBodyLens<ViewModel> = defaultViewLens): Response = this.status(Status.OK, viewLens)

fun ViewModel.status(
    status: Status,
    viewLens: BiDiBodyLens<ViewModel> = defaultViewLens,
): Response {
    return Response(status).with(viewLens of this)
}

open class ViewResponse(private val template: String) : ViewModel {
    override fun template() = template
}
