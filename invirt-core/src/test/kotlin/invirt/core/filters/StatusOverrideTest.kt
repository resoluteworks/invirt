package invirt.core.filters

import invirt.core.GET
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus

class StatusOverrideTest : StringSpec({

    "forbidden as not found" {
        val httpHandler = StatusOverride(Status.FORBIDDEN to Status.NOT_FOUND)
            .then(
                org.http4k.routing.routes(
                    "/test" GET { org.http4k.core.Response(Status.FORBIDDEN) }
                )
            )

        httpHandler(Request(Method.GET, "/test")) shouldHaveStatus Status.NOT_FOUND
    }

    "not overriding when status is not mapped" {
        val httpHandler = StatusOverride(Status.FORBIDDEN to Status.NOT_FOUND)
            .then(
                org.http4k.routing.routes(
                    "/test" GET { org.http4k.core.Response(Status.BAD_REQUEST) }
                )
            )

        httpHandler(Request(Method.GET, "/test")) shouldHaveStatus Status.BAD_REQUEST
    }
})
