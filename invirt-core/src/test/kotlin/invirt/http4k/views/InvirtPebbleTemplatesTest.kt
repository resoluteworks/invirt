package invirt.http4k.views

import invirt.http4k.GET
import invirt.http4k.InvirtFilter
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

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
})
