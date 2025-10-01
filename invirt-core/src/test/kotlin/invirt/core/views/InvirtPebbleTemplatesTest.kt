package invirt.core.views

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class InvirtPebbleTemplatesTest : StringSpec({

    "request object in pebble templates" {
        val handler = Invirt().then(
            routes(
                "/test" GET { renderTemplate(it, "invirt-pebble-filter-request-object") }
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
        val handler = Invirt().then(
            routes(
                "/test" GET { renderTemplate(it, "not-existent-template") }
            )
        )
        shouldThrow<InvirtException> {
            handler(Request(Method.GET, "/test"))
        }
    }
})
