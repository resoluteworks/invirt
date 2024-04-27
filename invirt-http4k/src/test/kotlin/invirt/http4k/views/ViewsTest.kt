package invirt.http4k.views

import invirt.http4k.GET
import invirt.http4k.Views
import invirt.http4k.renderTemplate
import invirt.http4k.setDefaultViewLens
import invirt.pebble.functions.NoArgsPebbleFunction
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.File

class ViewsTest : StringSpec() {

    init {
        "Views.HotReload" {
            val templateFile = File("src/test/resources/hot-reload-views/hot-reload-template.peb")
            val initialContent = templateFile.readText()
            try {
                val viewLens = Views.HotReload("src/test/resources/hot-reload-views")

                val httpHandler = routes("/test" GET { renderTemplate("hot-reload-template", viewLens) })
                httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe initialContent

                val updatedContent = uuid7()
                templateFile.writeText(updatedContent)
                httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe updatedContent
            } finally {
                templateFile.writeText(initialContent)
            }
        }

        "renderTemplate" {
            val viewLens = Views.Classpath("webapp/views")
            val httpHandler = routes(
                "/test" GET { renderTemplate("render-template", viewLens) }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "Spiceworld!"
        }

        "ViewModel.ok" {
            val viewLens = Views.Classpath("webapp/views")
            val httpHandler = routes(
                "/test" bind Method.GET to {
                    renderTemplate("view-model-ok", viewLens)
                }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "Cashback!"
        }

        "custom pebble extensions" {
            setDefaultViewLens(Views.Classpath("webapp/views", CustomExtension()))
            val httpHandler = routes(
                "/test" bind Method.GET to {
                    renderTemplate("custom-pebble-extension")
                }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "John Smith"
        }
    }
}

private class CurrentUsernameFunction : NoArgsPebbleFunction("currentUsername") {

    override fun execute(args: MutableMap<String, Any>?, self: PebbleTemplate?, context: EvaluationContext?, lineNumber: Int): Any {
        return "John Smith"
    }
}

private class CustomExtension : AbstractExtension() {
    override fun getFunctions(): Map<String, Function> {
        return listOf(
            CurrentUsernameFunction()
        ).associateBy { it.name }
    }
}
