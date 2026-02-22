package invirt.core.views

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtPebbleConfig
import invirt.pebble.functions.pebbleFunction
import invirt.pebble.pebbleFunctions
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.File

class ViewsTest : StringSpec() {

    init {
        "hot reload views" {
            val templateFile = File("src/test/resources/hot-reload-views/hot-reload-template.peb")
            val initialContent = templateFile.readText()
            try {
                Invirt.configure(
                    developmentMode = true,
                    pebble = InvirtPebbleConfig(hotReloadDirectory = "src/test/resources/hot-reload-views")
                )
                val httpHandler = routes("/test" GET { renderTemplate(it, "hot-reload-template") })
                httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe initialContent

                val updatedContent = uuid7()
                templateFile.writeText(updatedContent)

                // The rendered content must be updated after the template file is updated
                httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe updatedContent
            } finally {
                templateFile.writeText(initialContent)
            }
        }

        "classpath views" {
            Invirt.configure(
                developmentMode = false,
                pebble = InvirtPebbleConfig(classpathLocation = "classpath-views")
            )

            val httpHandler = routes("/test" GET { renderTemplate(it, "classpath-view") })
            httpHandler(Request(Method.GET, "/test")).bodyString().trim() shouldBe "Classpath view content"
        }

        "renderTemplate" {
            Invirt.configure()
            val httpHandler = routes(
                "/test" GET { renderTemplate(it, "render-template") }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "Spiceworld!"
        }

        "renderTemplate with model" {
            Invirt.configure()
            val httpHandler = routes("/test" bind Method.GET to { renderTemplate(it, "map-view", mapOf("key" to "test-value")) })

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "test-value"
        }

        "ViewModel.ok" {
            Invirt.configure()
            val httpHandler = routes(
                "/test" bind Method.GET to {
                    renderTemplate(it, "view-model-ok")
                }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "Cashback!"
        }

        "custom pebble extensions" {
            Invirt.configure(
                developmentMode = false,
                pebble = InvirtPebbleConfig(
                    classpathLocation = "webapp/views",
                    extensions = listOf(
                        pebbleFunctions(
                            pebbleFunction("currentUsername") {
                                "John Smith"
                            }
                        )
                    )
                )
            )
            val httpHandler = routes(
                "/test" bind Method.GET to {
                    renderTemplate(it, "custom-pebble-extension")
                }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trim() shouldBe "John Smith"
        }
    }
}
