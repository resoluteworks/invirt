package invirt.http4k.views

import invirt.http4k.GET
import invirt.http4k.InvirtFilter
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes
import org.http4k.template.ViewNotFound
import org.http4k.template.viewModel
import java.io.File

class InvirtPebbleTemplatesTest : StringSpec({

    beforeSpec { initialiseInvirtViews() }

    "request object in pebble templates" {
        val handler = InvirtFilter().then(
            routes(
                "/test" GET { renderTemplate("invirt-pebble-filter-request-object") }
            )
        )

        val response = handler(Request(Method.GET, "/test?q=pizza"))
        response shouldHaveStatus Status.OK
        response shouldHaveBody """
            GET
            /test?q=pizza
            pizza
        """.trimIndent()
    }

    "view not found error" {
        val handler = InvirtFilter().then(
            routes(
                "/test" GET { renderTemplate("not-existent-template") }
            )
        )
        shouldThrow<ViewNotFound> {
            handler(Request(Method.GET, "/test"))
        }
    }

    "caching directory" {
        val templateFile = File("src/test/resources/caching-dir-views/caching-dir-template.peb")
        val initialContent = templateFile.readText()
        try {
            val pebbleTemplates = pebbleTemplates("1.0", emptyList())
            defaultViewLens = Body.viewModel(pebbleTemplates.Caching("src/test/resources/caching-dir-views"), TEXT_HTML).toLens()

            val httpHandler = routes("/test" GET { renderTemplate("caching-dir-template") })
            httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe initialContent

            templateFile.writeText(uuid7())

            // The rendered content isn't updated after the template file is updated
            httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe initialContent
        } finally {
            templateFile.writeText(initialContent)
        }
    }
})
