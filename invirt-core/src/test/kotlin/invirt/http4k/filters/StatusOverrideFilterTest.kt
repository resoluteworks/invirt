package invirt.http4k.filters

import invirt.http4k.GET
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus

class StatusOverrideFilterTest : StringSpec({

    "forbidden as not found" {
        val httpHandler = StatusOverride(Status.FORBIDDEN to Status.NOT_FOUND)
            .then(
                org.http4k.routing.routes(
                    "/test" GET { org.http4k.core.Response(Status.FORBIDDEN) }
                )
            )

        httpHandler(Request(Method.GET, "/test")) shouldHaveStatus Status.NOT_FOUND
    }
})
